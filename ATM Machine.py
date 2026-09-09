import tkinter as tk
from tkinter import messagebox


# Store PIN and Balance
pin = ""
balance = 0


# -------------------------------
# CREATE ACCOUNT
# -------------------------------

def create_account():

    global pin, balance

    user_pin = pin_entry.get()
    user_balance = balance_entry.get()

    # Check PIN
    if len(user_pin) != 4 or not user_pin.isdigit():
        messagebox.showerror("Error", "PIN must be exactly 4 digits.")
        return

    # Check Balance
    if not user_balance.isdigit():
        messagebox.showerror("Error", "Please enter a valid balance.")
        return

    pin = user_pin
    balance = int(user_balance)

    setup_window.destroy()

    login_window()


# -------------------------------
# LOGIN
# -------------------------------

def login_window():

    login_window = tk.Tk()

    login_window.title("ATM MACHINE - Login")
    login_window.geometry("400x300")
    login_window.config(bg="#101820")
    login_window.resizable(False, False)

    tk.Label(
        login_window,
        text="ATM MACHINE",
        font=("Arial", 25, "bold"),
        fg="#00ff88",
        bg="#101820"
    ).pack(pady=30)

    tk.Label(
        login_window,
        text="Enter your PIN",
        font=("Arial", 14),
        fg="white",
        bg="#101820"
    ).pack()

    pin_box = tk.Entry(
        login_window,
        show="*",
        font=("Arial", 18),
        justify="center"
    )

    pin_box.pack(pady=15)


    def login():

        if pin_box.get() == pin:

            login_window.destroy()
            atm_window()

        else:

            messagebox.showerror(
                "Login Failed",
                "Wrong PIN!"
            )


    tk.Button(
        login_window,
        text="LOGIN",
        command=login,
        font=("Arial", 13, "bold"),
        bg="#00c853",
        fg="white",
        width=15,
        height=2
    ).pack()


    login_window.mainloop()


# -------------------------------
# ATM WINDOW
# -------------------------------

def atm_window():

    global balance

    atm = tk.Tk()

    atm.title("ATM MACHINE")
    atm.geometry("500x600")
    atm.config(bg="#101820")
    atm.resizable(False, False)


    # ATM Heading
    tk.Label(
        atm,
        text="ATM MACHINE",
        font=("Arial", 28, "bold"),
        fg="#00ff88",
        bg="#101820"
    ).pack(pady=25)


    # Balance Display
    balance_label = tk.Label(
        atm,
        text="Balance: Rs. " + format(balance, ","),
        font=("Arial", 18, "bold"),
        fg="white",
        bg="#17232c",
        width=30,
        height=2
    )

    balance_label.pack(pady=15)


    # Update Balance
    def update_balance():

        balance_label.config(
            text="Balance: Rs. " + format(balance, ",")
        )


    # -------------------------------
    # CHECK BALANCE
    # -------------------------------

    def check_balance():

        messagebox.showinfo(
            "Balance",
            "Current Balance:\n\nRs. "
            + format(balance, ",")
        )

        update_balance()


    # -------------------------------
    # DEPOSIT
    # -------------------------------

    def deposit():

        global balance

        deposit_window = tk.Toplevel(atm)

        deposit_window.title("Deposit Money")
        deposit_window.geometry("350x220")
        deposit_window.config(bg="#101820")
        deposit_window.resizable(False, False)


        tk.Label(
            deposit_window,
            text="Enter Deposit Amount",
            font=("Arial", 15, "bold"),
            fg="white",
            bg="#101820"
        ).pack(pady=20)


        amount_box = tk.Entry(
            deposit_window,
            font=("Arial", 18),
            justify="center"
        )

        amount_box.pack()


        def add_money():

            global balance

            amount = amount_box.get()

            if not amount.isdigit():

                messagebox.showerror(
                    "Error",
                    "Enter numbers only."
                )

                return


            amount = int(amount)


            if amount <= 0:

                messagebox.showerror(
                    "Error",
                    "Amount must be greater than 0."
                )

                return


            balance = balance + amount

            update_balance()


            messagebox.showinfo(
                "Deposit Successful",
                "Deposited: Rs. "
                + format(amount, ",")
                + "\n\nNew Balance: Rs. "
                + format(balance, ",")
            )


            deposit_window.destroy()


        tk.Button(
            deposit_window,
            text="DEPOSIT",
            command=add_money,
            bg="#00c853",
            fg="white",
            font=("Arial", 12, "bold"),
            width=15
        ).pack(pady=20)


    # -------------------------------
    # WITHDRAW
    # -------------------------------

    def withdraw():

        global balance

        withdraw_window = tk.Toplevel(atm)

        withdraw_window.title("Withdraw Money")
        withdraw_window.geometry("350x220")
        withdraw_window.config(bg="#101820")
        withdraw_window.resizable(False, False)


        tk.Label(
            withdraw_window,
            text="Enter Withdrawal Amount",
            font=("Arial", 15, "bold"),
            fg="white",
            bg="#101820"
        ).pack(pady=20)


        amount_box = tk.Entry(
            withdraw_window,
            font=("Arial", 18),
            justify="center"
        )

        amount_box.pack()


        def take_money():

            global balance

            amount = amount_box.get()


            if not amount.isdigit():

                messagebox.showerror(
                    "Error",
                    "Enter numbers only."
                )

                return


            amount = int(amount)


            if amount <= 0:

                messagebox.showerror(
                    "Error",
                    "Amount must be greater than 0."
                )

                return


            if amount > balance:

                messagebox.showerror(
                    "Insufficient Balance",
                    "You don't have enough balance."
                )

                return


            balance = balance - amount

            update_balance()


            messagebox.showinfo(
                "Withdrawal Successful",
                "Withdrawn: Rs. "
                + format(amount, ",")
                + "\n\nRemaining Balance: Rs. "
                + format(balance, ",")
            )


            withdraw_window.destroy()


        tk.Button(
            withdraw_window,
            text="WITHDRAW",
            command=take_money,
            bg="#e53935",
            fg="white",
            font=("Arial", 12, "bold"),
            width=15
        ).pack(pady=20)


    # -------------------------------
    # CHANGE PIN
    # -------------------------------

    def change_pin():

        global pin

        change_window = tk.Toplevel(atm)

        change_window.title("Change PIN")
        change_window.geometry("350x280")
        change_window.config(bg="#101820")
        change_window.resizable(False, False)


        tk.Label(
            change_window,
            text="CHANGE PIN",
            font=("Arial", 18, "bold"),
            fg="#00ff88",
            bg="#101820"
        ).pack(pady=15)


        tk.Label(
            change_window,
            text="Current PIN",
            fg="white",
            bg="#101820"
        ).pack()


        old_pin_box = tk.Entry(
            change_window,
            show="*",
            font=("Arial", 15),
            justify="center"
        )

        old_pin_box.pack(pady=5)


        tk.Label(
            change_window,
            text="New PIN",
            fg="white",
            bg="#101820"
        ).pack()


        new_pin_box = tk.Entry(
            change_window,
            show="*",
            font=("Arial", 15),
            justify="center"
        )

        new_pin_box.pack(pady=5)


        def update_pin():

            global pin

            old_pin = old_pin_box.get()
            new_pin = new_pin_box.get()


            if old_pin != pin:

                messagebox.showerror(
                    "Error",
                    "Current PIN is incorrect."
                )

                return


            if len(new_pin) != 4 or not new_pin.isdigit():

                messagebox.showerror(
                    "Error",
                    "New PIN must be exactly 4 digits."
                )

                return


            pin = new_pin


            messagebox.showinfo(
                "Success",
                "PIN changed successfully!"
            )


            change_window.destroy()


        tk.Button(
            change_window,
            text="CHANGE PIN",
            command=update_pin,
            bg="#2979ff",
            fg="white",
            font=("Arial", 12, "bold"),
            width=15
        ).pack(pady=15)


    # -------------------------------
    # LOGOUT
    # -------------------------------

    def logout():

        result = messagebox.askyesno(
            "Logout",
            "Do you want to logout?"
        )

        if result:
            atm.destroy()


    # -------------------------------
    # ATM BUTTONS
    # -------------------------------

    button_font = ("Arial", 13, "bold")


    tk.Button(
        atm,
        text="CHECK BALANCE",
        command=check_balance,
        bg="#00897b",
        fg="white",
        font=button_font,
        width=22,
        height=2
    ).pack(pady=8)


    tk.Button(
        atm,
        text="DEPOSIT MONEY",
        command=deposit,
        bg="#00c853",
        fg="white",
        font=button_font,
        width=22,
        height=2
    ).pack(pady=8)


    tk.Button(
        atm,
        text="WITHDRAW MONEY",
        command=withdraw,
        bg="#e53935",
        fg="white",
        font=button_font,
        width=22,
        height=2
    ).pack(pady=8)


    tk.Button(
        atm,
        text="CHANGE PIN",
        command=change_pin,
        bg="#2979ff",
        fg="white",
        font=button_font,
        width=22,
        height=2
    ).pack(pady=8)


    tk.Button(
        atm,
        text="LOGOUT",
        command=logout,
        bg="#6d4c41",
        fg="white",
        font=button_font,
        width=22,
        height=2
    ).pack(pady=8)


    atm.mainloop()


# ==========================================
# MAIN WINDOW
# ==========================================

setup_window = tk.Tk()

setup_window.title("ATM MACHINE")
setup_window.geometry("450x400")
setup_window.config(bg="#101820")
setup_window.resizable(False, False)


tk.Label(
    setup_window,
    text="ATM MACHINE",
    font=("Arial", 28, "bold"),
    fg="#00ff88",
    bg="#101820"
).pack(pady=30)


tk.Label(
    setup_window,
    text="Create Your 4-Digit PIN",
    font=("Arial", 13),
    fg="white",
    bg="#101820"
).pack()


pin_entry = tk.Entry(
    setup_window,
    show="*",
    font=("Arial", 18),
    justify="center"
)

pin_entry.pack(pady=10)


tk.Label(
    setup_window,
    text="Enter Starting Balance",
    font=("Arial", 13),
    fg="white",
    bg="#101820"
).pack()


balance_entry = tk.Entry(
    setup_window,
    font=("Arial", 18),
    justify="center"
)

balance_entry.pack(pady=10)


tk.Button(
    setup_window,
    text="CREATE ACCOUNT",
    command=create_account,
    font=("Arial", 13, "bold"),
    bg="#00c853",
    fg="white",
    width=20,
    height=2
).pack(pady=25)


setup_window.mainloop()
