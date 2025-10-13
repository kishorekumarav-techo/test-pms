import threading
import time
import sqlite3
from concurrent.futures import ThreadPoolExecutor

class ThreadMaker(threading.Thread):
    def __init__(self, id):
        super().__init__()
        self.id = id

    def run(self):
        for i in range(5):
            self.execute_query(i)
            time.sleep(2)

    def execute_query(self, param):
        for _ in range(10):
            conn = sqlite3.connect(":memory:")
            cursor = conn.cursor()
            cursor.execute("CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, val TEXT)")
            cursor.execute("INSERT INTO test (val) VALUES ('example')")
            cursor.execute("SELECT * FROM test WHERE id = ?", (param,))
            cursor.fetchall()
            cursor.close()
            conn.close()

class Worker:
    def __init__(self, id):
        self.id = id

    def perform(self):
        for i in range(5):
            self.run_query(i)
            time.sleep(1.5)

    def run_query(self, parameter):
        conn = sqlite3.connect(":memory:")
        cursor = conn.cursor()
        cursor.execute("CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, val TEXT)")
        cursor.execute("INSERT INTO test (val) VALUES ('sample')")
        cursor.execute("SELECT val FROM test WHERE id = ?", (parameter,))
        cursor.fetchall()
        cursor.close()
        conn.close()

def main():
    threads = [ThreadMaker(i) for i in range(5)]
    for t in threads:
        t.start()

    with ThreadPoolExecutor(max_workers=3) as executor:
        workers = [Worker(i) for i in range(5)]
        for w in workers:
            executor.submit(w.perform)

    for t in threads:
        t.join()

if __name__ == "__main__":
    main()
