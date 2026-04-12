from django.shortcuts import render, redirect, get_object_or_404
from .models import Post
from django.contrib.auth.forms import UserCreationForm, AuthenticationForm
from django.contrib.auth import authenticate, login, logout
from django.core.paginator import Paginator
from django.contrib.auth.decorators import login_required
from .forms import PostForm

# Home page view
def home(request):
    query = request.GET.get('q')
    if query:
        blogs = Post.objects.filter(title__icontains=query).order_by('-created_at')
    else:
        blogs = Post.objects.all().order_by('-created_at')

    paginator = Paginator(blogs, 4)
    page_number = request.GET.get('page')
    ap = paginator.get_page(page_number)

    return render(request, 'home.html', {'ap': ap, 'query': query})

# About page
def about(request):
    return render(request, 'about.html')

# Contact page
def contact(request):
    return render(request, 'contact.html')

# Register/Signup page
def register(request):
    if request.method == "POST":
        form = UserCreationForm(request.POST)
        if form.is_valid():
            form.save()
            return redirect('thank')
    else:
        form = UserCreationForm()
    return render(request, 'register.html', {'f': form})

# Thank you page
def thank(request):
    return render(request, 'thank.html')

# Login page
def loginpage(request):
    if request.method == 'POST':
        form = AuthenticationForm(request, data=request.POST)
        if form.is_valid():
            login(request, form.get_user())
            return redirect('dashboard')
    else:
        form = AuthenticationForm()
    return render(request, 'loginpage.html', {'f': form})

# Logout
def logoutpage(request):
    logout(request)
    return redirect('/')

# Dashboard page
@login_required
def dashboard(request):
    user_blogs = Post.objects.filter(user=request.user).order_by('-created_at')
    return render(request, 'dashboard.html', {'user_blogs': user_blogs})

# Blog detail
def blog_detail(request, blog_id):
    blog = get_object_or_404(Post, id=blog_id)
    return render(request, 'blog_detail.html', {'n': blog})

# Add Blog
@login_required
def add_blog(request):
    if request.method == 'POST':
        form = PostForm(request.POST, request.FILES)
        if form.is_valid():
            blog = form.save(commit=False)
            blog.user = request.user
            blog.save()
            return redirect('dashboard')
    else:
        form = PostForm()
    return render(request, 'add_blog.html', {'form': form})

# Edit Blog
@login_required
def edit_blog(request, blog_id):
    blog = get_object_or_404(Post, id=blog_id, user=request.user)
    if request.method == 'POST':
        form = PostForm(request.POST, request.FILES, instance=blog)
        if form.is_valid():
            form.save()
            return redirect('dashboard')
    else:
        form = PostForm(instance=blog)
    return render(request, 'edit_blog.html', {'form': form})

# Delete Blog
@login_required
def delete_blog(request, blog_id):
    blog = get_object_or_404(Post, id=blog_id, user=request.user)
    if request.method == 'POST':
        blog.delete()
        return redirect('dashboard')
    return render(request, 'delete_blog.html', {'blog': blog})
