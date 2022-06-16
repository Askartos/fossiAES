# fossiAES project

Hardware implementation of the ECB mode of advanced encryption standard (AES) 128bit. In Chisel.

![aes diagram](https://user-images.githubusercontent.com/51058246/172271861-db12ddd8-03e9-4202-89fd-bdb0ac956a37.png)

The functionality was validated against software implementation Tiny-AES library (https://github.com/kokke/tiny-AES-c), running on the caravel core (VexRiscv). The simulation results not only show that the block works, but also show a 100000X improvement in terms of encryption rate. (102.4 Mbps  vs 1.023 Kbps)

# Registers Map:
        Address   regiser
        ----------------------
        0x3000000 word[31:0]
        0x3000004 word[63:32]
        0x3000008 word[95:64]
        0x300000C word[127:96]
        
        0x3000010 key[31:0]
        0x3000014 key[63:32]
        0x3000018 key[95:64]
        0x300001C key[127:96]
        
        0x3000020 start_bit
        0x3000024 finish_bit
# Code example
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
        void main(int argc, char** argv) {	
          int key[] = {0x00010203, 0x04050607, 0x08090a0b, 0x0c0d0e0f};
          int word[] = {0x00112233, 0x44556677, 0x8899aabb, 0xccddeeff};
          aes_crypt(key , word);
        }
# to re-generate for the verilog aes
        need to have sbt installed on your system
        modify the scala files verilog/rtl/aes/src/main/scala/* if needed
        cd verilog/rtl/aes
        ./run_chisel.sh
# Layout 
![final_layout](https://user-images.githubusercontent.com/51058246/172377979-e42c2293-6e49-4b4f-b249-963d19f71cc6.png)

