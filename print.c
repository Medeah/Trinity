#include <stdio.h>
#include <math.h>
#include <stdbool.h>
#define IDX2C(i,j,ld) (((j)*(ld))+(i))
float *test;
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

int
main (void)
{
  float _u0[2];
  _u0[0] = 1;
  _u0[1] = 2;
  test = _u0;
  print_m (test, 1, 2);
  print_s (2 + 2);
  print_b (2 == 3);
  print_s (sin (1));
  sin (3);
  return 0;
};
