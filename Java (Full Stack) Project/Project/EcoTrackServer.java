import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
public class EcoTrackServer {
    static final int PORT=8787;
    static final Path DATA=Paths.get("data");
    static final Path USERS=DATA.resolve("users.json");
    static final Path CALCS=DATA.resolve("calculations.json");
    static final Map<String,String> sessions=new java.util.concurrent.ConcurrentHashMap<>();
    public static void main(String[] args)throws Exception {
        Files.createDirectories(DATA);
        if(!Files.exists(USERS)) Files.writeString(USERS,"[]");
        if(!Files.exists(CALCS)) Files.writeString(CALCS,"[]");
        HttpServer s=HttpServer.create(new InetSocketAddress(PORT),0);
        s.createContext("/api",EcoTrackServer::api);
        s.createContext("/",EcoTrackServer::staticFiles);
        s.setExecutor(Executors.newCachedThreadPool());
        s.start();
        System.out.println("EcoTrack is running at http://localhost:"+PORT);
        System.out.println("Press Ctrl+C to stop the server.");
    }
    static final Map<String,String> PAGE_ROUTES=Map.ofEntries(         Map.entry("/", "index.html"),         Map.entry("/home", "home.html"),         Map.entry("/dashboard", "dashboard.html"),         Map.entry("/about", "about.html"),         Map.entry("/register", "register.html"),         Map.entry("/login", "login.html"),         Map.entry("/forgot-password", "forgot-password.html"),         Map.entry("/tracking", "tracking.html"),         Map.entry("/transport", "transport.html"),         Map.entry("/home-energy", "home-energy.html"),         Map.entry("/food", "food.html"),         Map.entry("/shopping", "shopping.html"),         Map.entry("/learn-more", "learn-more.html")     );
    static final Map<String,String> LEGACY_PAGE_REDIRECTS=Map.ofEntries(         Map.entry("/index.html", "/"),         Map.entry("/home.html", "/home"),         Map.entry("/dashboard.html", "/dashboard"),         Map.entry("/about.html", "/about"),         Map.entry("/register.html", "/register"),         Map.entry("/login.html", "/login"),         Map.entry("/forgot-password.html", "/forgot-password"),         Map.entry("/tracking.html", "/tracking"),         Map.entry("/transport.html", "/transport"),         Map.entry("/home-energy.html", "/home-energy"),         Map.entry("/food.html", "/food"),         Map.entry("/shopping.html", "/shopping"),         Map.entry("/learn-more.html", "/learn-more")     );
    static void staticFiles(HttpExchange x)throws IOException {
        String requested=URLDecoder.decode(x.getRequestURI().getPath(),StandardCharsets.UTF_8);
        String redirect=LEGACY_PAGE_REDIRECTS.get(requested);
        if(redirect!=null) {
            x.getResponseHeaders().set("Location", redirect);
            x.sendResponseHeaders(302, -1);
            x.close();
            return;
        }
        String fileName=PAGE_ROUTES.get(requested);
        String resourcePath=fileName!=null?fileName:(requested.startsWith("/")?requested.substring(1):requested);
        if(resourcePath.isEmpty())resourcePath="index.html";
        Path f=Paths.get(".").resolve(resourcePath).normalize();
        if(!Files.exists(f)||Files.isDirectory(f)) {
            send(x,404,"text/plain","Not found");
            return;
        }
        sendBytes(x,200,contentType(resourcePath),Files.readAllBytes(f));
    }
    static String contentType(String path) {
        String p=path.toLowerCase(Locale.ROOT);
        if(p.endsWith(".html"))return "text/html; charset=UTF-8";
        if(p.endsWith(".css"))return "text/css; charset=UTF-8";
        if(p.endsWith(".js"))return "application/javascript; charset=UTF-8";
        if(p.endsWith(".json"))return "application/json; charset=UTF-8";
        if(p.endsWith(".png"))return "image/png";
        if(p.endsWith(".jpg")||p.endsWith(".jpeg"))return "image/jpeg";
        if(p.endsWith(".svg"))return "image/svg+xml";
        if(p.endsWith(".ico"))return "image/x-icon";
        return "application/octet-stream";
    }
    static void api(HttpExchange x)throws IOException {
        String p=x.getRequestURI().getPath(), method=x.getRequestMethod();
        try {
            if(p.equals("/api/register")&&method.equals("POST")) {
                register(x);
                return;
            }
            if(p.equals("/api/login")&&method.equals("POST")) {
                login(x);
                return;
            }
            if(p.equals("/api/reset-password")&&method.equals("POST")) {
                resetPassword(x);
                return;
            }
            if(p.equals("/api/logout")&&method.equals("POST")) {
                logout(x);
                return;
            }
            if(p.equals("/api/delete-account")&&method.equals("POST")) {
                deleteAccount(x);
                return;
            }
            if(p.equals("/api/me")&&method.equals("GET")) {
                me(x);
                return;
            }
            if(p.equals("/api/calculate")&&method.equals("POST")) {
                calculate(x);
                return;
            }
            if(p.equals("/api/dashboard")&&method.equals("GET")) {
                dashboard(x);
                return;
            }
            send(x,404,"application/json","{\"message\":\"API route not found\"}");
        }
        catch(Exception e) {
            send(x,500,"application/json","{\"message\":\""+esc(e.getMessage()==null?"Server error":e.getMessage())+"\"}");
        }
    }
    static String body(HttpExchange x)throws IOException {
        return new String(x.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
    }
    static void register(HttpExchange x)throws Exception {
        Map<String,String> j=parse(body(x));
        String name=j.get("name"),email=j.get("email"),pass=j.get("password");
        if(name==null||name.isBlank()||email==null||!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")||pass==null||pass.length()<6) {
            send(x,400,"application/json","{\"message\":\"Please enter a name, valid email and password of at least 6 characters.\"}");
            return;
        }
        List<String> users=records(USERS);
        for(String u:users)if(email.equalsIgnoreCase(get(u,"email"))) {
            send(x,409,"application/json","{\"message\":\"Email is already registered.\"}");
            return;
        }
        String rec="{\"name\":\""+esc(formatName(name))+"\",\"email\":\""+esc(email.toLowerCase(Locale.ROOT))+"\",\"password\":\""+hashPassword(pass)+"\"}";
        users.add(rec);
        writeRecords(USERS,users);
        send(x,200,"application/json","{\"message\":\"Registration successful\"}");
    }
    static void login(HttpExchange x)throws Exception {
        Map<String,String> j=parse(body(x));
        String email=j.get("email"),pass=j.get("password");
        String found=null;
        String hashedPass = hashPassword(pass == null ? "" : pass);
        for(String u:records(USERS))if(email!=null&&email.equalsIgnoreCase(get(u,"email"))&&Objects.equals(hashedPass,get(u,"password"))) {
            found=get(u,"email");
            break;
        }
        if(found==null) {
            send(x,401,"application/json","{\"message\":\"Invalid email or password.\"}");
            return;
        }
        String sid=UUID.randomUUID().toString();
        sessions.put(sid,found);
        String uname="";
        for(String u:records(USERS))if(found.equalsIgnoreCase(get(u,"email"))) {
            uname=get(u,"name");
            break;
        }
        x.getResponseHeaders().add("Set-Cookie","ECOTRACK_SESSION="+sid+"; Path=/; HttpOnly; SameSite=Lax");
        send(x,200,"application/json","{\"message\":\"Login successful\",\"user\":{\"name\":\""+esc(uname)+"\",\"email\":\""+esc(found)+"\"}}");
    }
    static void resetPassword(HttpExchange x)throws Exception {
        Map<String,String> j=parse(body(x));
        String email=j.get("email"),newPass=j.get("password"),confirm=j.get("confirmPassword");
        if(email==null||!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            send(x,400,"application/json","{\"message\":\"Please enter a valid registered email.\"}");
            return;
        }
        if(newPass==null||newPass.length()<6) {
            send(x,400,"application/json","{\"message\":\"Password must be at least 6 characters.\"}");
            return;
        }
        if(!Objects.equals(newPass,confirm)) {
            send(x,400,"application/json","{\"message\":\"Passwords do not match.\"}");
            return;
        }
        List<String> users=records(USERS);
        boolean updated=false;
        for(int i=0;i<users.size();i++) {
            String u=users.get(i);
            if(email.equalsIgnoreCase(get(u,"email"))) {
                String name=get(u,"name");
                users.set(i,"{\"name\":\""+esc(name==null?"":name)+"\",\"email\":\""+esc(email.toLowerCase(Locale.ROOT))+"\",\"password\":\""+esc(hashPassword(newPass))+"\"}");
                updated=true;
                break;
            }
        }
        if(!updated) {
            send(x,404,"application/json","{\"message\":\"No account found with this email.\"}");
            return;
        }
        writeRecords(USERS,users);
        send(x,200,"application/json","{\"message\":\"Password updated successfully.\"}");
    }
    static void logout(HttpExchange x)throws IOException {
        String sid=cookie(x,"ECOTRACK_SESSION");
        if(sid!=null)sessions.remove(sid);
        x.getResponseHeaders().add("Set-Cookie","ECOTRACK_SESSION=; Max-Age=0; Path=/");
        send(x,200,"application/json","{\"message\":\"Logged out\"}");
    }
    static void deleteAccount(HttpExchange x)throws Exception {
        String user=current(x);
        if(user==null) {
            send(x,401,"application/json","{\"message\":\"Please login first.\"}");
            return;
        }
        List<String> users=records(USERS);
        users.removeIf(u->user.equalsIgnoreCase(get(u,"email")));
        writeRecords(USERS,users);
        List<String> cs=records(CALCS);
        cs.removeIf(c->user.equalsIgnoreCase(get(c,"email")));
        writeRecords(CALCS,cs);
        String sid=cookie(x,"ECOTRACK_SESSION");
        if(sid!=null)sessions.remove(sid);
        x.getResponseHeaders().add("Set-Cookie","ECOTRACK_SESSION=; Max-Age=0; Path=/");
        send(x,200,"application/json","{\"message\":\"Account deleted successfully.\"}");
    }
    static void me(HttpExchange x)throws Exception {
        String u=current(x);
        if(u==null) {
            send(x,200,"application/json","{\"loggedIn\":false}");
            return;
        }
        String name="";
        for(String a:records(USERS))if(u.equalsIgnoreCase(get(a,"email"))) {
            name=get(a,"name");
            break;
        }
        send(x,200,"application/json","{\"loggedIn\":true,\"name\":\""+esc(name)+"\",\"email\":\""+esc(u)+"\"}");
    }
    static void calculate(HttpExchange x)throws Exception {
        String user=current(x);
        if(user==null) {
            send(x,401,"application/json","{\"message\":\"Please login first.\"}");
            return;
        }
        Map<String,String> j=parse(body(x));
        double transport=0,home=0,food=0,shop=0;
        String vehicle=j.getOrDefault("vehicle","None");
        double car=n(j,"carKm"),bike=n(j,"bikeKm"),bus=n(j,"busKm"),train=n(j,"trainKm");
        double carFactor=vehicle.startsWith("Electric")?.05:vehicle.startsWith("Diesel")?.19:vehicle.startsWith("Petrol")||vehicle.startsWith("Petrol /")?.17:0;
        transport+=car*52*carFactor + bike*52*.11 + bus*52*.06 + train*52*.04 + n(j,"shortFlights")*.25+n(j,"longFlights")*1.1;
        home+=n(j,"electricity")*12*.42+n(j,"gas")*12*2.0+n(j,"lpg")*12*3.0;
        String ren=j.getOrDefault("renewable","None");
        if(ren.equals("Some"))home*=.8;
        else if(ren.equals("Mostly"))home*=.5;
        else if(ren.equals("100% renewable"))home*=.12;
        food+=n(j,"beef")*52*.27+n(j,"lamb")*52*.24+n(j,"pork")*52*.08+n(j,"chicken")*52*.05+n(j,"fish")*52*.04;
        String diet=j.getOrDefault("diet","Mixed");
        if(diet.equals("Vegetarian"))food*=.55;
        else if(diet.equals("Vegan"))food*=.35;
        else if(diet.equals("Pescatarian"))food*=.65;
        String waste=j.getOrDefault("foodWaste","Medium");
        if(waste.equals("High"))food*=1.15;
        else if(waste.equals("Low"))food*=.9;
        shop=n(j,"clothes")*12*.04+n(j,"electronics")*.12+n(j,"newItems")*12*.02;
        String sec=j.getOrDefault("secondhand","Rarely");
        if(sec.equals("Sometimes"))shop*=.8;
        else if(sec.equals("Often"))shop*=.6;
        transport/=1000;
        home/=1000;
        food/=1000;
        shop/=1000;
        double total=transport+home+food+shop;
        String rec="{\"email\":\""+esc(user)+"\",\"savedAt\":\""+Instant.now()+"\",\"total\":"+total+",\"breakdown\":{\"Transport\":"+transport+",\"Home Energy\":"+home+",\"Food & Diet\":"+food+",\"Shopping\":"+shop+"}}";
        List<String> cs=records(CALCS);
        cs.removeIf(a->user.equalsIgnoreCase(get(a,"email")));
        cs.add(rec);
        writeRecords(CALCS,cs);
        send(x,200,"application/json","{\"message\":\"Calculation saved\",\"total\":"+total+"}");
    }
    static void dashboard(HttpExchange x)throws Exception {
        String user=current(x);
        if(user==null) {
            send(x,401,"application/json","{\"message\":\"Please login first.\"}");
            return;
        }
        String c=null;
        for(String a:records(CALCS))if(user.equalsIgnoreCase(get(a,"email")))c=a;
        if(c==null) {
            send(x,200,"application/json","{\"calculation\":null}");
            return;
        }
        double total=Double.parseDouble(getNested(c,"total")),t=Double.parseDouble(getNested(c,"Transport")),h=Double.parseDouble(getNested(c,"Home Energy")),f=Double.parseDouble(getNested(c,"Food & Diet")),s=Double.parseDouble(getNested(c,"Shopping"));
        List<String> tips=new ArrayList<>();
        if(t>=Math.max(h,Math.max(f,s)))tips.add("Transport is your largest category. Try public transport, carpooling or fewer car kilometres each week.");
        if(h>=Math.max(t,Math.max(f,s)))tips.add("Home energy is your largest category. Reduce electricity use and consider renewable energy.");
        if(f>=Math.max(t,Math.max(h,s)))tips.add("Food is your largest category. Reducing high-impact meat meals can lower your footprint.");
        if(s>=Math.max(t,Math.max(h,f)))tips.add("Shopping is your largest category. Prefer durable and second-hand products.");
        if(tips.isEmpty())tips.add("Your footprint is spread across categories. Small improvements in each area can add up.");
        if(total<3)tips.add("Your estimated footprint is relatively low. Keep the habits that are working well.");
        StringBuilder a=new StringBuilder("{\"calculation\":").append(c).append(",\"insights\":[");
        for(int i=0;i<tips.size();i++) {
            if(i>0)a.append(",");
            a.append("\"").append(esc(tips.get(i))).append("\"");
        }
        a.append("]}");
        send(x,200,"application/json",a.toString());
    }
    static String current(HttpExchange x) {
        String sid=cookie(x,"ECOTRACK_SESSION");
        return sid==null?null:sessions.get(sid);
    }
    static String cookie(HttpExchange x,String name) {
        String h=x.getRequestHeaders().getFirst("Cookie");
        if(h==null)return null;
        for(String q:h.split(";")) {
            String[] z=q.trim().split("=",2);
            if(z.length==2&&z[0].equals(name))return z[1];
        }
        return null;
    }
    static double n(Map<String,String>j,String k) {
        try {
            return Math.max(0,Double.parseDouble(j.getOrDefault(k,"0")));
        }
        catch(Exception e) {
            return 0;
        }
    }

    static List<String> records(Path p)throws IOException {
        String s=Files.readString(p);
        List<String> out=new ArrayList<>();
        int depth=0,start=-1;
        boolean in=false;
        for(int i=0;i<s.length();i++) {
            char c=s.charAt(i);
            if(c=='"'&&(i==0||s.charAt(i-1)!='\\'))in=!in;
            if(!in) {
                if(c=='{') {
                    if(depth==0)start=i;
                    depth++;
                }
                else if(c=='}') {
                    depth--;
                    if(depth==0&&start>=0) {
                        out.add(s.substring(start,i+1));
                        start=-1;
                    }
                }
            }
        }
        return out;
    }
    static void writeRecords(Path p,List<String>r)throws IOException {
        Files.writeString(p,"["+String.join(",",r)+"]");
    }
    static String get(String j,String k) {
        String q="\""+k+"\":\"";
        int i=j.indexOf(q);
        if(i<0)return null;
        i+=q.length();
        int e=j.indexOf("\"",i);
        while(e>i&&j.charAt(e-1)=='\\')e=j.indexOf("\"",e+1);
        return e<0?null:j.substring(i,e);
    }
    static String getNested(String j,String k) {
        String q="\""+k+"\":";
        int i=j.indexOf(q);
        if(i<0)return"0";
        i+=q.length();
        int e=i;
        while(e<j.length()&&",}".indexOf(j.charAt(e))<0)e++;
        return j.substring(i,e);
    }
    static Map<String,String> parse(String s) {
        Map<String,String>m=new HashMap<>();
        MatcherLite mm=new MatcherLite(s);
        while(mm.next()) {
            m.put(mm.key,mm.value);
        }
        return m;
    }
    // Store the password exactly as entered by the user.
    static String hashPassword(String password) {
        return password == null ? "" : password;
    }

    static String formatName(String value) {
        String trimmed=value==null?"":value.trim();
        if(trimmed.isEmpty()) return "";
        StringBuilder out=new StringBuilder();
        for(String part:trimmed.split("\\s+")) {
            if(part.isEmpty()) continue;
            if(out.length()>0) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0)));
            if(part.length()>1) out.append(part.substring(1));
        }
        return out.toString();
    }
    static String esc(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ");
    }
    static void send(HttpExchange x,int code,String type,String body)throws IOException {
        sendBytes(x,code,type+"; charset=utf-8",body.getBytes(StandardCharsets.UTF_8));
    }
    static void sendBytes(HttpExchange x,int code,String type,byte[] bytes)throws IOException {
        x.getResponseHeaders().set("Content-Type",type);
        x.sendResponseHeaders(code,bytes.length);
        try(OutputStream o=x.getResponseBody()) {
            o.write(bytes);
        }
    }
    static class MatcherLite {
        String s;
        int p=0;
        String key,value;
        MatcherLite(String s) {
            this.s=s;
        }
        boolean next() {
            while(p<s.length()&&s.charAt(p)!='\"')p++;
            if(p>=s.length())return false;
            int a=++p,b=s.indexOf('"',a);
            if(b<0)return false;
            key=s.substring(a,b);
            p=b+1;
            while(p<s.length()&&s.charAt(p)!=':')p++;
            p++;
            while(p<s.length()&&Character.isWhitespace(s.charAt(p)))p++;
            if(p<s.length()&&s.charAt(p)=='"') {
                a=++p;
                b=p;
                while(b<s.length()) {
                    if(s.charAt(b)=='"'&&s.charAt(b-1)!='\\')break;
                    b++;
                }
                value=s.substring(a,b);
                p=b+1;
            }
            else {
                a=p;
                while(p<s.length()&&",}".indexOf(s.charAt(p))<0)p++;
                value=s.substring(a,p).trim();
            }
            return true;
        }
    }
}
