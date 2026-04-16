import random
import tkinter as tk

class GuessGame:
    def __init__(self, root):
        self.root = root
        self.root.title("Infinite Guess Game 🔥")
        self.root.geometry("320x250")

        # 🔥 no limit number (very large range)
        self.number = random.randint(-10**9, 10**9)
        self.attempts = 0

        tk.Label(root, text="Guess ANY number (Infinite range)").pack(pady=10)

        self.entry = tk.Entry(root)
        self.entry.pack()

        tk.Button(root, text="Guess", command=self.check).pack(pady=5)

        self.result = tk.Label(root, text="")
        self.result.pack(pady=10)

        tk.Button(root, text="Restart", command=self.reset).pack()

    def check(self):
        try:
            guess = int(self.entry.get())
            self.attempts += 1

            if guess < self.number:
                self.result.config(text="📉 Too Low!")
            elif guess > self.number:
                self.result.config(text="📈 Too High!")
            else:
                self.result.config(text=f"🎉 Correct! Attempts: {self.attempts}")

        except:
            self.result.config(text="❌ Enter valid number")

    def reset(self):
        self.number = random.randint(-10**9, 10**9)
        self.attempts = 0
        self.result.config(text="")
        self.entry.delete(0, tk.END)

root = tk.Tk()
game = GuessGame(root)
root.mainloop()