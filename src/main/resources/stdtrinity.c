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
	float* resMatrix = (float*)malloc(rowsA * colsA * sizeof(float));

	for (i = 0; i < rowsA * colsA; i++) {
		resMatrix[i] = s * A[i];
	}

	return resMatrix;
}

float* mfdiv(float s, float* A, size_t rowsA, size_t colsA) {
	int i;
	float* resMatrix = (float*)malloc(rowsA * colsA * sizeof(float));

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
	float* resMatrix = (float*)malloc(nrRowsA * nrColsA * sizeof(float));

    for(n = 0; n < nrRowsA * nrColsA; n++) {
        int i = n / nrRowsA;
        int j = n % nrRowsA;
        resMatrix[n] = A[nrColsA * j + i];
    }

    return resMatrix;
}



float* eye(size_t size) {
	int i;

	float* C = (float*)calloc(size * size, sizeof(float));

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
	float* resMatrix = (float*)malloc(rows * cols * sizeof(float));

	for (i = 0; i < rows * cols; i++) {
		resMatrix[i] = A[i] + B[i];
	}

	return resMatrix;
}

float* mmsubt(float* A, float* B, size_t rows, size_t cols) {
	int i;
	float* resMatrix = (float*)malloc(rows * cols * sizeof(float));

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
