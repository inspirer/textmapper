// pool.h

class Pool {
private:
	struct Chunk {
		Chunk *next;
		void *memory;
		int size;
	};

	Chunk *root;

public:
	Pool() {
		root = NULL;
	}
	~Pool() {
		release();
	}

	void *allocate(int size) {
		Chunk *chunk = new Chunk;
		chunk->next = root;
		chunk->memory = malloc(size);
		chunk->size = size;
		root = chunk;
		return chunk->memory;
	}

	void *resize( void *mem, int new_size ) {
		Chunk *iterator = root;
		while( iterator ) {
			if( iterator->memory == mem ) {
				iterator->memory = realloc(mem, new_size);
				return iterator->memory;
			}
			iterator = iterator->next;
		}
		return allocate(new_size);
	}

	void release() {
		Chunk *iterator = root;
		while( iterator ) {
			Chunk *current = iterator;
			iterator = iterator->next;
			free(current->memory);
			delete current;
		}
		root = NULL;
	}
};

template <class T>
class Array {
private:
	T* value;
	int count, allocated;

	// no copies allowed
	Array<T>(const Array<T> &arr) {}
	Array<T>& operator=(const Array<T> &arr) {}

public:
	Array<T>(Pool *pool) {
		allocated = 64 * sizeof(T);
		value = (T *)pool->allocate(allocated);
		count = 0;
	}

	T& operator[](int pos) {
		return value[pos];
	}
};
