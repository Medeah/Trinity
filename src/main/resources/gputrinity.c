#include <cuda.h>
#include <cuda_runtime.h>
#include <cublas_v2.h>

void gpu_blas_mmul(const float *A, const float *B, float *C, const int m, const int k, const int n);

float* mmmult(const float *A, size_t rowsA, size_t colsA, const float *B, size_t rowsB, size_t colsB) {
	float *d_A, *d_B, *d_temp;
	int rowsC = rowsA, colsC = colsB;
	float *C = (float*)malloc(rowsC * colsC * sizeof(float));
	cudaError_t error;

	// Allocate memory on Device
	error = cudaMalloc(&d_A, rowsA * colsA * sizeof(float));
	if (error != cudaSuccess) {
		fprintf(stderr, "Memory was not allocated for matrix A");
		exit(EXIT_FAILURE);
	}

	error = cudaMalloc(&d_B, rowsB * colsB * sizeof(float));
	if (error != cudaSuccess) {
		fprintf(stderr, "Memory was not allocated for matrix B");
		exit(EXIT_FAILURE);
	}

	error = cudaMalloc(&d_temp, rowsC * colsC * sizeof(float));
	if (error != cudaSuccess) {
		fprintf(stderr, "Memory was not allocated for matrix C");
		exit(EXIT_FAILURE);
	}

	// Copy h_A and h_B to the device
	error = cudaMemcpy(d_A, A, rowsA * colsA * sizeof(float), cudaMemcpyHostToDevice);
	if (error != cudaSuccess) {
		fprintf(stderr, Copying matrice h_A HtoD failed");
		exit(EXIT_FAILURE);
	}

	error = cudaMemcpy(d_B, B, rowsB * colsB * sizeof(float), cudaMemcpyHostToDevice);
	if (error != cudaSuccess){
		fprintf(stderr, Copying matrice h_B HtoD failed");
		exit(EXIT_FAILURE);
	}

	// Multiplication on the device
	gpu_blas_mmul(d_A, d_B, d_temp, rowsA, colsB, colsA);

	// Copy result back to the host
	float *h_temp = (float*)malloc(rowsA * colsB * sizeof(float));

	error = cudaMemcpy(h_temp, d_temp, rowsC * colsC * sizeof(float), cudaMemcpyDeviceToHost);
	if (error != cudaSuccess){
		fprintf(stderr, "Copying matrix d_C DtoH failed iteration");
		exit(EXIT_FAILURE);
	}

	// Copy h_temp which is column-major to h_C which is row-major
	int n;
	for (n = 0; n < rowsC * colsC; n++) {
		int i = n / colsC;
		int j = n % colsC;
		C[n] = h_temp[rowsC * j + i];
	}

	free(h_temp);

	cudaFree(d_A);
	cudaFree(d_B);
	cudaFree(d_temp);
	cudaDeviceReset();

	return C;
}

// m = rowsArowsC, k = colsArowsB, n = colsBcolsC
void gpu_blas_mmul(const float *A, const float *B, float *C, const int m, const int n, const int k) {
	int lda = k, ldb = n, ldc = m;
	const float alf = 1;
	const float bet = 0;
	const float *alpha = &alf;
	const float *beta = &bet;

	// Create a handle for CUBLAS
	cublasHandle_t handle;
	cublasCreate(&handle);

	// Do the actual multiplication
	if (cublasSgemm(handle, CUBLAS_OP_T, CUBLAS_OP_T, m, n, k, alpha, A, lda, B, ldb, beta, C, ldc) != CUBLAS_STATUS_SUCCESS){
		printf("cublasSgemm failed");
	}

	// Destroy the handle
	cublasDestroy(handle);
}