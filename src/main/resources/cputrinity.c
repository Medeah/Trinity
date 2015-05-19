float* mmmult(const float* A, size_t rowsA, size_t colsA, const float* B, size_t rowsB, size_t colsB) {
	int rowsC, colsC, indexA, indexB, crA, ccA, crB, ccB, Cindex = 0;
	rowsC = rowsA;
	colsC = colsB;
	float sum;
	float* C = (float*)malloc(rowsC * colsC * sizeof(float));

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
}