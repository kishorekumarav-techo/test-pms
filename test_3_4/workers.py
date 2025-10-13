import threading
import sqlite3
import time
from concurrent.futures import ThreadPoolExecutor

class TaskThread(threading.Thread):
    def __init__(self, identifier):
        super().__init__()
        self.identifier = identifier

    def run(self):
        for i in range(5):
            self.db_query(i)
            time.sleep(2)

    def db_query(self, idx):
        for _ in range(10):
            conn = sqlite3.connect(":memory:")
            cursor = conn.cursor()
            cursor.execute("CREATE TABLE IF NOT EXISTS sample (id INTEGER PRIMARY KEY, value TEXT)")
            cursor.execute("INSERT INTO sample (value) VALUES ('test')")
            cursor.execute("SELECT * FROM sample WHERE id = ?", (idx,))
            cursor.fetchall()
            cursor.close()
            conn.close()

class Worker:
    def __init__(self, identifier):
        self.identifier = identifier

    def execute(self):
        for i in range(5):
            self.make_query(i)
            time.sleep(1.5)

    def make_query(self, val):
        conn = sqlite3.connect(":memory:")
        cursor = conn.cursor()
        cursor.execute("CREATE TABLE IF NOT EXISTS sample (id INTEGER PRIMARY KEY, value TEXT)")
        cursor.execute("INSERT INTO sample (value) VALUES ('worker')")
        cursor.execute("SELECT value FROM sample WHERE id = ?", (val,))
        cursor.fetchall()
        cursor.close()
        conn.close()

def main():
    threads = [TaskThread(i) for i in range(5)]
    for t in threads:
        t.start()

    with ThreadPoolExecutor(max_workers=3) as executor:
        workers = [Worker(i) for i in range(5)]
        for w in workers:
            executor.submit(w.execute)

    for t in threads:
        t.join()

if __name__ == "__main__":
    main()
