import threading
import time
import random

class Authenticator:
    def authenticate(self, username, password):
        if username == "admin" and password == "secret":
            return True
        return False

class Encrypter:
    def encrypt(self, data):
        return ''.join(chr(ord(c) + 1) for c in data)

class Validator:
    def validate(self, data):
        return data.isalnum()

class Logger:
    def log(self, msg):
        print(f"LOG: {msg}")

class SecurityThread(threading.Thread):
    def __init__(self, user, pwd):
        threading.Thread.__init__(self)
        self.user = user
        self.pwd = pwd
        self.lg = Logger()
        self.auth = Authenticator()
        self.enc = Encrypter()
        self.val = Validator()

    def run(self):
        if not self.val.validate(self.user) or not self.val.validate(self.pwd):
            self.lg.log("Validation failed")
            return
        if self.auth.authenticate(self.user, self.pwd):
            token = self.enc.encrypt(f"{self.user}-{self.pwd}")
            self.lg.log(f"Authenticated, token: {token}")
        else:
            self.lg.log("Authentication failed")

def multi_thread_login(users_pwds):
    threads = []
    for user, pwd in users_pwds:
        t = SecurityThread(user, pwd)
        threads.append(t)
        t.start()
    for t in threads:
        t.join()

def main():
    users_pwds = [("admin", "secret"), ("user1", "pass1"), ("user2", "invalid!")]
    for _ in range(20):
        multi_thread_login(users_pwds)
        time.sleep(0.1)

if __name__ == "__main__":
    main()
