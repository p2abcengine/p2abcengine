all:
	hcl -DSODER -g -O main.c -o main_soder.hzx
	hcl -g -O main.c -o main_patras.hzx

alu: all
	halugen main_soder.hzx
	halugen main_patras.hzx

debug: 
	hcl -DTEST_MODE -v -g -O main.c -o main_test.hzx
	halugen main_test.hzx
	hcl -DTEST_PATRAS_MODE -v -g -O main.c -o main_test_patras.hzx
	halugen main_test_patras.hzx
