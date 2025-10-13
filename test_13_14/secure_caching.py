import threading
import time
import random

class SecurityCache:
    _cache = {}
    _cache_lock = threading.Lock()

    @classmethod
    def get_cached_data(cls, key, user):
        if not user or 'role' not in user or user['role'] != 'admin':
            return None
        with cls._cache_lock:
            return cls._cache.get(key)

    @classmethod
    def cache_data(cls, key, value):
        with cls._cache_lock:
            cls._cache[key] = value

    @classmethod
    def clear_cache(cls):
        with cls._cache_lock:
            cls._cache.clear()

class ComplexProcessor:
    def __init__(self, data):
        self.data = data

    def process(self):
        result = {}
        for idx, item in enumerate(self.data):
            if idx % 2 == 0:
                if item > 10:
                    if item < 20:
                        result[idx] = item * 2
                    else:
                        if item < 30:
                            result[idx] = item * 3
                        else:
                            result[idx] = item * 4
                else:
                    if item == 5:
                        result[idx] = 100
                    else:
                        if item == 1:
                            result[idx] = 50
                        else:
                            result[idx] = item
            else:
                if item < 0:
                    result[idx] = 0
                else:
                    result[idx] = item
        return result

def expensive_operation(input_data):
    time.sleep(0.05)
    return {str(i): input_data[i] * random.random() for i in range(len(input_data))}

def authorized_data_fetch(user, input_data):
    cached = SecurityCache.get_cached_data('expensive', user)
    if cached is not None:
        return cached

    result = expensive_operation(input_data)
    SecurityCache.cache_data('expensive', result)
    return result

def handle_request(user, data):
    if not user:
        return None
    if 'active' not in user or not user['active']:
        return None

    processor = ComplexProcessor(data)
    processed = processor.process()
    return authorized_data_fetch(user, list(processed.values()))

def main():
    users = [
        {'name': 'Alice', 'role': 'admin', 'active': True},
        {'name': 'Bob', 'role': 'user', 'active': False},
        {'name': 'Eve', 'role': 'admin', 'active': False},
    ]
    data = [5, 15, 25, 35, 1, 0, -5, 10]

    for _ in range(20):
        for user in users:
            res = handle_request(user, data)
            time.sleep(0.01)

if __name__ == "__main__":
    main()
