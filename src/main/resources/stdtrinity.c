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

bool print_m(float *m, int r, int c) {
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

float* fmmult(float s, float* A, int rowsA, int colsA) {
	int i;
	float* resMatrix;
	resMatrix = malloc(rowsA * colsA * sizeof(float));

	for (i = 0; i < rowsA * colsA; i++) {
		resMatrix[i] = s * A[i];
	}

	return resMatrix;
} /* TODO: call free() on matrix resMatrix */

float* mfdiv(float s, float* A, int rowsA, int colsA) {
	int i;
	float* resMatrix;
	resMatrix = malloc(rowsA * colsA * sizeof(float));

	if (s != 0){
    		for (i = 0; i < rowsA * colsA; i++) {
    			resMatrix[i] = A[i] / s;
    			//printf("%.1f \n", B[i]);
    		}
    	} else {
    		stdError("It is impossible to divide by", s);
    	}

	return resMatrix;
} /* TODO: call free() on matrix resMatrix */

float* transpose(float* A, int nrRowsA, int nrColsA) {
	int n;
	float* resMatrix;
	resMatrix = malloc(nrRowsA * nrColsA * sizeof(float));

    for(n = 0; n < nrRowsA * nrColsA; n++) {
        int i = n / nrRowsA;
        int j = n % nrRowsA;
        resMatrix[n] = A[nrColsA * j + i];
    }

    return resMatrix;
}/* TODO: call free() on matrix resMatrix */

float* mmmult(float* A, int rowsA, int colsA, float* B, int rowsB, int colsB) {
	int rowsC, colsC, indexA, indexB, crA, ccA, crB, ccB, Cindex = 0;
	rowsC = rowsA;
	colsC = colsB;
	float sum;
	float* C;
	C = malloc(rowsC * colsC * sizeof(float));

	for (crA = 0; crA < rowsA; crA++) {
		for (ccB = 0; ccB < colsB; ccB++) {
			sum = 0;
			for (ccA = 0; ccA < colsA; ccA++) {
				crB = ccA;
				sum += A[crA * colsA + ccA] * B[crB * colsB + ccB];
			}
		C[Cindex] = sum;
		Cindex += 1;
		}
	}

	return C;
}/* TODO: call free on C */

float* eye(size_t size) {
	int i, j;

	float* C;
	C = calloc(size * size, sizeof(float));

	for (i = 0; i < size; i++) {
			C[i * (size + 1)] = 1.0f;
	}

	return C;
}

float* mfexpo(float* A, size_t size, float exponent) {
	int j, expo;
	float sum;
	float *C = malloc(size * size * sizeof(float));

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

float* mmadd(float* A, float* B, int rows, int cols) {
	int i;
	float* resMatrix;
	resMatrix = malloc(rows * cols * sizeof(float));

	for (i = 0; i < rows * cols; i++) {
		resMatrix[i] = A[i] + B[i];
	}

	return resMatrix;
}

float* mmsubt(float* A, float* B, int rows, int cols) {
	int i;
	float* resMatrix;
	resMatrix = malloc(rows * cols * sizeof(float));

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
