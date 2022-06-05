#include <defs.h>
#include <stub.c>
#include <aes.h>

#define aes ((volatile int *) 0x30000000)
void aes_crypt(int* key, int* word){
	//Word
	aes[0] = word[0];
  aes[1] = word[1];
	aes[2] = word[2];
	aes[3] = word[3]; 
	//Key
	aes[4] = key[0];
	aes[5] = key[1];
	aes[6] = key[2];
	aes[7] = key[3];

	//Start aes
	aes[8] = 0x1;
	
	while(aes[9]==0x00000000); //aes finish
	
	//Word
	word[0] = aes[0];
  word[1] = aes[1];
	word[2] = aes[2];
	word[3] = aes[3]; 
}

void caravel_setup(){


    reg_spi_enable = 1;
    reg_wb_enable = 1; //needed to enable  wb transactions

    reg_mprj_io_31 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_30 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_29 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_28 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_27 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_26 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_25 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_24 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_23 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_22 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_21 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_20 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_19 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_18 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_17 = GPIO_MODE_MGMT_STD_OUTPUT;
    reg_mprj_io_16 = GPIO_MODE_MGMT_STD_OUTPUT;

    reg_mprj_xfer = 1;
    while (reg_mprj_xfer == 1);

		reg_la2_oenb = reg_la2_iena = 0x00000000;    // [95:64]

    // Flag start of the test
		reg_mprj_datal = 0xAB600000;

}

void main(int argc, char** argv) {	
  caravel_setup();

//AES HARDWARE 
	int key[] = {0x00010203, 0x04050607, 0x08090a0b, 0x0c0d0e0f};
	int word[] = {0x00112233, 0x44556677, 0x8899aabb, 0xccddeeff};
	aes_crypt(key , word);

	reg_mprj_datal = 0xCAFE0000;

//AES SOFTWARE
	struct AES_ctx ctx;
	uint8_t aeskey[]	= { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
	uint8_t str[] 		= { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xaa, 0xbb, 0xcc, 0xdd, 0xee, 0xff };
	AES_init_ctx(&ctx, aeskey);
	AES_ECB_encrypt(&ctx, str);
  
 reg_mprj_datal= 0;
 bool pass=true;
 char count=0;	    
 int a,b;
 uint8_t i, k;	
	for ( k= 0; (k < 4) && pass; ++k) {	
		for ( i= 3; (i >= 0) && pass; --i) {
	      reg_mprj_datal = a = (( word[k] >> i*8 ) & 0xFF) << 24;
	      reg_mprj_datal = 0;
   			reg_mprj_datal = b = ((int) str[count])					<< 24;
				if(a!=b) pass	 = false;
				count++;
	 	}
	}
	
	
 
	// Flag end of the test
	if(pass) reg_mprj_datal = 0xAB610000;

}


