#include <stdio.h>
#include <math.h>
#include <stdbool.h>
#include <stdlib.h>


/* Index to Rows */
#define IDX2R(i,j,ld) (((i)*(ld))+(j))
/* Index to Columns */
#define IDX2C(i,j,ld) (((j)*(ld))+(i))
/* Index to FORTAN (Columns with 1-indexing)*/
#define IDX2F(i,j,ld) ((((j)-1)*(ld))+((i)-1))
/* Index to Trinity (Rows with 1-indexing)*/
#define IDX2T(i,j,ld) ((((i)-1)*(ld))+((j)-1))

/* Prototype for the mmult call in mfexpo()*/
float* mmmult(const float *A, size_t rowsA, size_t colsA, const float *B, size_t rowsB, size_t colsB);

bool print_m(float *m, size_t r, size_t c) {
   int i, j;
   for(i = 0; i < r; i++) {
     printf("[%f", m[IDX2R(i, 0, c)]);
     for(j = 1; j < c ; j++) {
       printf(", %f", m[IDX2R(i, j, c)]);
     }
     printf("]\n");
   }
   return true;
 }

bool print_b(bool b) {
  if (b) {
    printf("true\n");
  } else {
    printf("false\n");
  }
  return true;
}

bool print_s(float s) {
  printf("%f\n", s);
  return true;
}

void stdError(char* errorString, int value) {
	fprintf(stderr, "%s: %d\n", errorString, value);
	exit(1);
}

float* fmmult(float s, float* A, size_t rowsA, size_t colsA) {
	int i;
	float* resMatrix = malloc(rowsA * colsA * sizeof(float));

	for (i = 0; i < rowsA * colsA; i++) {
		resMatrix[i] = s * A[i];
	}

	return resMatrix;
}

float* mfdiv(float s, float* A, size_t rowsA, size_t colsA) {
	int i;
	float* resMatrix = malloc(rowsA * colsA * sizeof(float));

    if (s != 0){
        for (i = 0; i < rowsA * colsA; i++) {
            resMatrix[i] = A[i] / s;
        }
    } else {
        stdError("It is impossible to divide by", s);
    }

	return resMatrix;
}

float* transpose(float* A, size_t nrRowsA, size_t nrColsA) {
	int n;
	float* resMatrix = malloc(nrRowsA * nrColsA * sizeof(float));

    for(n = 0; n < nrRowsA * nrColsA; n++) {
        int i = n / nrRowsA;
        int j = n % nrRowsA;
        resMatrix[n] = A[nrColsA * j + i];
    }

    return resMatrix;
}



float* eye(size_t size) {
	int i;

	float* C = calloc(size * size, sizeof(float));

	for (i = 0; i < size; i++) {
			C[i * (size + 1)] = 1.0f;
	}

	return C;
}

float* mfexpo(float* A, size_t size, float exponent) {
	int j, expo;
	float sum;
	float *C = (float*)malloc(size * size * sizeof(float));

	/* rounding of the exponent. Decimal number not currently supported.*/
	expo = round(exponent);

	if (expo == 1) {
		return A;
	}

	if (expo == 0) {
		C = eye(size);
		return C;
	}

    for(j = 0; j < size * size; j++) {
    	C[j] = A[j];
    }

	if (expo > 1) {
		while (expo > 1) {
			C = mmmult(C, size, size, A, size, size);
			expo = expo - 1;
		}
	} else {
		stdError("The exponential function cannot be calculated with negative exponent value", exponent);
	}

	return C;
}

float dotProduct(float* A, float* B, size_t size) {
	float sum = 0.0f;
	int i;

	for (i = 0; i < size; i++) {
		sum += A[i] * B[i];
	}

	return sum;
}

bool mmeq(float* A, float* B, size_t rows, size_t cols) {
	int i;

	for (i = 0; i < rows * cols; i++) {
		if (A[i] != B[i]) {
			return false;
		}
	}
	return true;
}

float* mmadd(float* A, float* B, size_t rows, size_t cols) {
	int i;
	float* resMatrix = malloc(rows * cols * sizeof(float));

	for (i = 0; i < rows * cols; i++) {
		resMatrix[i] = A[i] + B[i];
	}

	return resMatrix;
}

float* mmsubt(float* A, float* B, size_t rows, size_t cols) {
	int i;
	float* resMatrix = malloc(rows * cols * sizeof(float));

	for (i = 0; i < rows * cols; i++) {
		resMatrix[i] = A[i] - B[i];
	}

	return resMatrix;
}

float _abs(float s) {
  return fabs(s);
}

float _round(float s) {
  return round(s);
}

float _floor(float s) {
  return floor(s);
}

float _ceil(float s) {
  return ceil(s);
}

float _sin(float s) {
  return sin(s);
}

float _cos(float s) {
  return cos(s);
}

float _tan(float s) {
  return tan(s);
}

float _asin(float s) {
  return asin(s);
}

float _acos(float s) {
  return acos(s);
}

float _atan(float s) {
  return atan(s);
}

float _log(float s) {
  return log(s);
}

float _log10(float s) {
  return log10(s);
}

float _sqrt(float s) {
  return sqrt(s);
}
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
		printf("Memory was not allocated for matrix A");
		exit(EXIT_FAILURE);
	}

	error = cudaMalloc(&d_B, rowsB * colsB * sizeof(float));
	if (error != cudaSuccess) {
		printf("Memory was not allocated for matrix B");
		exit(EXIT_FAILURE);
	}

	error = cudaMalloc(&d_temp, rowsC * colsC * sizeof(float));
	if (error != cudaSuccess) {
		printf("Memory was not allocated for matrix C");
		exit(EXIT_FAILURE);
	}

	//Copy h_A and h_B to the device
	error = cudaMemcpy(d_A, A, rowsA * colsA * sizeof(float), cudaMemcpyHostToDevice);
	if (error != cudaSuccess) {
		printf("Copying matrice h_A HtoD failed");
		exit(EXIT_FAILURE);
	}

	error = cudaMemcpy(d_B, B, rowsB * colsB * sizeof(float), cudaMemcpyHostToDevice);
	if (error != cudaSuccess){
		printf("Copying matrice h_B HtoD failed");
		exit(EXIT_FAILURE);
	}

	// Multiplication on the device
	gpu_blas_mmul(d_A, d_B, d_temp, rowsA, colsB, colsA);

	//Copy result back to the host
	float *h_temp = (float*)calloc(rowsA * colsB, sizeof(float));

	error = cudaMemcpy(h_temp, d_temp, rowsC * colsC * sizeof(float), cudaMemcpyDeviceToHost);
	if (error != cudaSuccess){
		printf("Copying matrix d_C DtoH failed iteration");
		exit(EXIT_FAILURE);
	}

	// Copy h_temp which is column-major to h_C which is row-major
	int n;
	for (n = 0; n < rowsC * colsC; n++) {
		int i = n / colsC;
		int j = n % colsC;
		C[n] = h_temp[rowsC * j + i];
	}

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
}float _s;int main(void){_s=4;print_s(_s);return 0;};