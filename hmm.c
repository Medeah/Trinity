#include <stdio.h>
#include <math.h>
#include <stdbool.h>

#define IDX2C(i,j,ld) (((j)*(ld))+(i))

bool print_m(float *m, int r, int c) {
  int i, j;
  for(i = 0; i < r; i++) {
    printf("[");
    for(j = 0; j < c-1; j++) {
      printf("%f, ", m[IDX2C(i, j, r)]);
    }
    printf("%f]\n", m[IDX2C(i, j, r)]);
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
  return log(s);
}

float _sqrt(float s) {
  return sqrt(s);
}


int main(void) {
  float matrix[6] = {11, 21, 12, 22, 13, 23};
  print_m(matrix,2 , 3);
  float matri[1] = {11};
  print_m(matri,1 , 1);
  float matr[2] = {11, 22};
  print_m(matr,1 , 2);
  print_m(matr,2 , 1);

  print_b(false);
  print_b(true);

  float f = -987.654321;
  print_s(f);
  print_s(_abs(f));

  print_s(_cos (f));
  print_s(_sqrt (100));

  return 0;
}
