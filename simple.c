#include <stdio.h>
#include <math.h>
#include <stdbool.h>
#include <omp.h>

#define IDX2C(i,j,ld) (((j)*(ld))+(i))

bool
print_m (float *m, int r, int c)
{
  int i, j;
  for (i = 0; i < r; i++)
    {
      printf ("[");
      for (j = 0; j < c - 1; j++)
	{
	  printf ("%f, ", m[IDX2C (i, j, r)]);
	}
      printf ("%f]\n", m[IDX2C (i, j, r)]);
    }
  return true;
}

bool
print_b (bool b)
{
  if (b)
    {
      printf ("true\n");
    }
  else
    {
      printf ("false\n");
    }
  return true;
}

bool
print_s (float s)
{
  printf ("%f\n", s);
  return true;
}

float *
fmmult (int s, float *A, int rowsA, int colsA)
{
  int i;
  float *resMatrix;
  resMatrix = malloc (rowsA * colsA * sizeof (float));

  for (i = 0; i < rowsA * colsA; i++)
    {
      resMatrix[i] = s * A[i];
    }

  return resMatrix;
}				// TODO: husk af kalde free() på matrix resMatrix

float *
transpose (float *A, int nrRowsA, int nrColsA)
{
  int n;
  float *resMatrix;
  resMatrix = malloc (nrRowsA * nrColsA * sizeof (float));

#pragma omp parallel for
  for (n = 0; n < nrRowsA * nrColsA; n++)
    {
      int i = n / nrRowsA;
      int j = n % nrRowsA;
      resMatrix[n] = A[nrColsA * j + i];
    }

  return resMatrix;
}				// TODO: husk af kalde free() på matrix resMatrix

float
_abs (float s)
{
  return fabs (s);
}

float
_round (float s)
{
  return round (s);
}

float
_floor (float s)
{
  return floor (s);
}

float
_ceil (float s)
{
  return ceil (s);
}

float
_sin (float s)
{
  return sin (s);
}

float
_cos (float s)
{
  return cos (s);
}

float
_tan (float s)
{
  return tan (s);
}

float
_asin (float s)
{
  return asin (s);
}

float
_acos (float s)
{
  return acos (s);
}

float
_atan (float s)
{
  return atan (s);
}

float
_log (float s)
{
  return log (s);
}

float
_log10 (float s)
{
  return log (s);
}

float
_sqrt (float s)
{
  return sqrt (s);
}

/* ENTRY POINT */
float *M;
float *N;
float *T;
float m;
float
a ()
{
  float ai = 2;
  return 4 + m;
}

float
b ()
{
  float bix = 2;
  return 3 + a ();
}

int
main (void)
{
  float _u0[6];
  _u0[0] = 2;
  _u0[1] = 4;
  _u0[2] = 6;
  _u0[3] = 3;
  _u0[4] = 5;
  _u0[5] = 1;
  M = _u0;
  N = fmmult (-1, M, 2, 3);
  T = transpose (M, 2, 3);
  m = 2;
  printf (Scalar);
  return 0;
};
