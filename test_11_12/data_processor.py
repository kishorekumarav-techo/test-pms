import threading
import time
import random
import requests

class DataProcessor:
    def __init__(self):
        self.data_cache = {}

    def expensive_calculation(self, x):
        result = 0
        for _ in range(1000):
            result += (x ** 2 + random.random())
        return result

    def process_data(self, values):
        output = []
        for val in values:
            temp = self.expensive_calculation(val)
            output.append(temp)
            unused_variable = 42
        return output

    def clear_cache(self):
        self.data_cache.clear()

def blocking_network_call():
    r = requests.get("https://httpbin.org/delay/5")
    return r.status_code

def worker_thread():
    processor = DataProcessor()
    for i in range(10):
        values = [random.randint(1, 100) for _ in range(50)]
        processor.process_data(values)
        time.sleep(2)
        blocking_network_call()

def main():
    threads = []
    for _ in range(5):
        t = threading.Thread(target=worker_thread)
        threads.append(t)
        t.start()
    for t in threads:
        t.join()

if __name__ == "__main__":
    main()
