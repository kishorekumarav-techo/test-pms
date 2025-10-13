import threading
import time
import random

class Keeper:
    def __init__(self):
        self.storage = {}
        self.lock = threading.Lock()

    def store_value(self, key, value):
        with self.lock:
            self.storage[key] = value

    def get_value(self, key):
        with self.lock:
            return self.storage.get(key)

    def clear(self):
        with self.lock:
            self.storage.clear()

class Storage:
    def __init__(self):
        self.data_map = {}
        self.data_lock = threading.Lock()

    def save(self, identifier, content):
        with self.data_lock:
            self.data_map[identifier] = content

    def retrieve(self, identifier):
        with self.data_lock:
            return self.data_map.get(identifier)

    def wipe(self):
        with self.data_lock:
            self.data_map = {}

class WorkerA(threading.Thread):
    def __init__(self, id, lock_obj, store_obj):
        threading.Thread.__init__(self)
        self.id = id
        self.lock_obj = lock_obj
        self.store_obj = store_obj

    def run(self):
        try:
            self.lock_obj.acquire()
            value = self.fetch_data()
            self.store_obj.store_value(self.id, value)
        finally:
            self.lock_obj.release()

    def fetch_data(self):
        return [random.randint(0, 1000) for _ in range(10000)]

class WorkerB(threading.Thread):
    def __init__(self, id, lock_obj, store_obj):
        threading.Thread.__init__(self)
        self.id = id
        self.lock_obj = lock_obj
        self.store_obj = store_obj

    def run(self):
        try:
            self.lock_obj.acquire()
            data = self.store_obj.retrieve(self.id)
            self.process_data(data)
        finally:
            self.lock_obj.release()

    def process_data(self, data):
        total = sum(data or [])
        for _ in range(50):
            time.sleep(0.01)
            result = self.complex_operation(total)
        return result

    def complex_operation(self, value):
        temp = value
        for _ in range(100):
            temp = temp * 1.0001
        return temp

def main():
    lock_list = [threading.Lock() for _ in range(3)]
    storage_obj = Storage()
    workers = []

    for i in range(10):
        tA = WorkerA(i, lock_list[0], storage_obj)
        tB = WorkerB(i, lock_list[1], storage_obj)
        workers.extend([tA, tB])

    for t in workers:
        t.start()

    for t in workers:
        t.join()
    print("Processing completed.")

if __name__ == "__main__":
    main()
