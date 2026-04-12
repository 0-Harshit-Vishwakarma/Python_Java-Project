import tkinter as tk

def add_task():
    task = entry.get()
    if task != "":
        listbox.insert(tk.END, task)
        entry.delete(0, tk.END)

def delete_task():
    selected = listbox.curselection()
    if selected:
        listbox.delete(selected)

# Window setup
root = tk.Tk()
root.title("Todo List App")
root.geometry("300x400")

# Input box
entry = tk.Entry(root, width=25)
entry.pack(pady=10)

# Buttons
add_btn = tk.Button(root, text="Add Task", command=add_task)
add_btn.pack()

delete_btn = tk.Button(root, text="Delete Task", command=delete_task)
delete_btn.pack(pady=5)

# Listbox
listbox = tk.Listbox(root, width=40, height=15)
listbox.pack(pady=10)

# Run app
root.mainloop()