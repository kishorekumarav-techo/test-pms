import threading
import random
import time

class DataChunkProcessor:
    def __init__(self, chunk):
        self.chunk = chunk

    def process(self):
        time.sleep(random.uniform(0.01, 0.03))
        return [x * random.random() for x in self.chunk]

class LargeBufferManager:
    def __init__(self, size):
        self.buffer = [random.randint(0, 1000) for _ in range(size)]

    def release(self):
        self.buffer = None

class SingletonCache:
    _instance = None
    data = {}

    @classmethod
    def get_instance(cls):
        if not cls._instance:
            cls._instance = SingletonCache()
        return cls._instance

    def set(self, key, val):
        self.data[key] = val

    def get(self, key):
        return self.data.get(key)

def generate_large_data(size):
    return [random.randint(1, 1000) for _ in range(size)]

class Worker(threading.Thread):
    def __init__(self, data, idx):
        threading.Thread.__init__(self)
        self.data = data
        self.idx = idx

    def run(self):
        total = 0
        for i in range(3):
            chunk = self.data[i*1000:(i+1)*1000]
            proc = DataChunkProcessor(chunk)
            output = proc.process()
            total += sum(output)
            for _ in range(8):
                check = LargeBufferManager(5000)
                total += sum(check.buffer[:5])
            check.release()

        cache = SingletonCache.get_instance()
        cache.set(self.idx, total)
        time.sleep(random.uniform(0.05, 0.09))

def main():
    threads = []
    large_data = generate_large_data(3000)
    for i in range(10):
        t = Worker(large_data, i)
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    cache = SingletonCache.get_instance()
    results = [cache.get(i) for i in range(10)]
    print("Final Results:", results)

if __name__ == "__main__":
    main()
