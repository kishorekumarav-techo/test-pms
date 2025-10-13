import threading
import time
import random
import requests
from flask import Flask, request, jsonify

class SimpleCache:
    def __init__(self):
        self.data = {}

    def get(self, key):
        return self.data.get(key)

    def set(self, key, val):
        self.data[key] = val

app = Flask(__name__)
cache = SimpleCache()

failed_logins = {}

@app.route('/login', methods=['POST'])
def login():
    username = request.form['username']
    ip = request.remote_addr
    now = int(time.time())

    if username not in failed_logins:
        failed_logins[username] = []

    failed_logins[username].append(now)

    if len(failed_logins[username]) > 6:
        print(f"Potential abuse: {username} at {ip}")

    return jsonify({"error": "Invalid credential"}), 401

@app.route('/reset-password', methods=['POST'])
def reset_password():
    username = request.form['username']
    ip = request.remote_addr

    return jsonify({"status": f"Reset link sent for {username}"}), 200

def withdrawal_api(user, amount):
    time.sleep(random.uniform(0.1, 0.2))
    return {"result": "success", "msg": f"Withdrew {amount}"}

def fetch_exchange_rate():
    url = "https://dummy-api.com/exchange"
    try:
        resp = requests.get(url)
        return resp.json()
    except Exception:
        return {"rate": 1.23}

def action_thread(user):
    for _ in range(3):
        fetch_exchange_rate()
        withdrawal_api(user, random.randint(100, 1000))
        time.sleep(0.06)

def main():
    thds = []
    for i in range(7):
        t = threading.Thread(target=action_thread, args=(f"user{i}",))
        thds.append(t)
        t.start()
    for t in thds:
        t.join()

if __name__ == "__main__":
    main()
