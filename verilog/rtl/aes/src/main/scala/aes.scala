/**********************************************************
*	Name:  				AES
*
*	Authors: 			Oscar Pardo & Hanssel Morales -2020
*
*	Modified by: 		Hanssel Morales-05/2022.												
*																													
* Abstract:		An AES accelerator
***********************************************************/

package fossiAES

import chisel3._
import chisel3.util._


class aes (val addrlen : Int ,val base:BigInt) extends Module{
	val selector: Boolean = false // true for more key regs, false charge the key everytime
	val io = IO(new whishbone_slave)
	
	
	 //registers
	val default = List("h0".U(32.W),"h0".U(32.W),"h0".U(32.W),"h0".U(32.W),"h0".U(32.W),"h0".U(32.W),"h0".U(32.W),"h0".U(32.W),"h0".U(1.W))
	val nrego = default.size
	val rego =RegInit(VecInit(default))
	val regi = Wire(Vec(1,UInt(addrlen.W)))
	val normregos = Wire(Vec(nrego,UInt(addrlen.W)))
	normregos:=rego
	val full_regs = Wire(Vec(4+nrego,UInt(addrlen.W)))
	full_regs:= (Cat(regi.asUInt,normregos.asUInt)).asTypeOf(full_regs) 
	

	val standBy :: stage0 :: stage1 :: stage2 :: stage3 :: stage4 :: stage5 :: stage6 :: stage7 :: stage8 :: stage9 :: Nil = Enum(11)
	val state = RegInit(standBy)
	val busy	= state =/= standBy

  val mask = Wire(Vec(4,UInt(8.W)))
	for (j <- 0 until 4) {
		 when(io.wbs_sel_i(j)){
  		mask(j):=0xFF.U
 		 }.otherwise{
 		 	mask(j):=0.U
 		 }
	}


  val addr = Wire(UInt((log2Ceil(1+nrego)).W))
	addr:= (io.wbs_adr_i-base.U) >>2 
	
	val valid = io.wbs_stb_i & !busy & ( (io.wbs_adr_i & (0xFF.U <<24) ) === ( base.U & (0xFF.U <<24)) )
	
	val ack = RegNext(valid)
 
 io.wbs_ack_o:=ack
 //write 
 when( valid & io.wbs_cyc_i & io.wbs_we_i ){
				rego(addr) := (rego(addr) & ! (mask.asUInt)) |  ( io.wbs_dat_i & mask.asUInt )
 }
 //read

	
 val readed = Wire(UInt(32.W))
 when( valid & io.wbs_cyc_i & ! io.wbs_we_i ){
		readed:=full_regs(addr)
 }.otherwise {
		readed:=0.U 
 }
 

	io.wbs_dat_o := RegNext(readed)


	val ronda 			= RegInit(0.U(4.W))
	val selMux1W0 	= RegInit(5.U(3.W))
	val selMux1W1 	= RegInit(5.U(3.W))
	val selMux1W2 	= RegInit(5.U(3.W))
	val selMux1W3 	= RegInit(5.U(3.W))
	val selMuxSbox	= RegInit(4.U(3.W))
	val selMuxMixARK= RegInit(0.U(2.W))
	val selKey			= RegInit(0.U(2.W))

	val ctrlGlobal = rego(8)
	val reg1W0 = rego(0)
	val reg1W1 = rego(1)
	val reg1W2 = rego(2)
	val reg1W3 = rego(3)

	val reg1K0 = if(selector) RegEnable(rego(4), 0.U, true.B) else rego(4)
	val reg1K1 = if(selector) RegEnable(rego(5), 0.U, true.B) else rego(5)
	val reg1K2 = if(selector) RegEnable(rego(6), 0.U, true.B) else rego(6)
	val reg1K3 = if(selector) RegEnable(rego(7), 0.U, true.B) else rego(7)

	val mux2Sbox = MuxCase(0.U,
 	  Array((selMuxSbox === 0.U) -> reg1W0,
 	        (selMuxSbox === 1.U) -> reg1W1,
 	        (selMuxSbox === 2.U) -> reg1W2,	
 	        (selMuxSbox === 3.U) -> reg1W3,
 	      	(selMuxSbox === 4.U) -> Cat(reg1K3(23,0),reg1K3(31,24))))
	val mux2MixARK = MuxCase(0.U,
 	  Array((selMuxMixARK === 0.U) -> reg1W0,
 	        (selMuxMixARK === 1.U) -> reg1W1,
 	        (selMuxMixARK === 2.U) -> reg1W2,	
 	        (selMuxMixARK === 3.U) -> reg1W3))

	val moduloSbox = Module(new sbox)
	moduloSbox.io.msg := mux2Sbox
	val moduloMix = Module(new mix)
	moduloMix.io.msg := mux2MixARK

	val cumbia = RegNext(moduloSbox.io.msg_out)

	val rcon_pure: Seq[Int] = Seq(
 	0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1B, 0x36, 0x6C, 0xD8, 0x00, 0x00, 0x00, 0x00)
 	val rcon_hw = VecInit(rcon_pure.map(_.U(8.W)))
	val put0 = Cat(rcon_hw(ronda),0.U(24.W)) ^ cumbia ^ reg1K0
	val put1 = put0 ^ reg1K1
	val put2 = put1 ^ reg1K2
	val put3 = put2 ^ reg1K3

	val mux1K0 = MuxCase(0.U,
		Array((selKey === 0.U) -> reg1K0,
				  (selKey === 1.U) -> put0))
	val mux1K1 = MuxCase(0.U,
		Array((selKey === 0.U) -> reg1K1,
				  (selKey === 1.U) -> put1))
	val mux1K2 = MuxCase(0.U,
		Array((selKey === 0.U) -> reg1K2,
				  (selKey === 1.U) -> put2))
	val mux1K3 = MuxCase(0.U,
		Array((selKey === 0.U) -> reg1K3,
			  	(selKey === 1.U) -> put3))

	val key_out = MuxCase(0.U,
 	  Array((selMuxMixARK === 0.U) -> reg1K0,
 	        (selMuxMixARK === 1.U) -> reg1K1,
 	        (selMuxMixARK === 2.U) -> reg1K2,	
 	        (selMuxMixARK === 3.U) -> reg1K3))
	val moduloARK = moduloMix.io.msg_out ^ key_out

	val arkW0 = reg1W0 ^ reg1K0
	val arkW1 = reg1W1 ^ reg1K1
	val arkW2 = reg1W2 ^ reg1K2
	val arkW3 = reg1W3 ^ reg1K3

	val auxiliary = key_out ^ moduloSbox.io.msg_out

	val mux1W0 = MuxCase(0.U,
 	  Array((selMux1W0 === 0.U) -> reg1W0,
 	        (selMux1W0 === 1.U) -> moduloSbox.io.msg_out,
 	        (selMux1W0 === 2.U) -> moduloARK,	
 	        (selMux1W0 === 3.U) -> Cat(reg1W0(31,24),reg1W1(23,16),reg1W2(15,8),moduloARK(7,0)),
 	        (selMux1W0 === 4.U) -> Cat(arkW0(31,24),arkW1(23,16),arkW2(15,8),arkW3(7,0)),
 	        (selMux1W0 === 5.U) -> auxiliary))
	val mux1W1 = MuxCase(0.U,
 	  Array((selMux1W1 === 0.U) -> reg1W1,
 	        (selMux1W1 === 1.U) -> moduloSbox.io.msg_out,
 	        (selMux1W1 === 2.U) -> moduloARK,	
 	        (selMux1W1 === 3.U) -> Cat(reg1W1(31,24),reg1W2(23,16),moduloARK(15,8),reg1W0(7,0)),
 	        (selMux1W0 === 4.U) -> Cat(arkW1(31,24),arkW2(23,16),arkW3(15,8),arkW0(7,0)),
 	        (selMux1W1 === 5.U) -> auxiliary))
	val mux1W2 = MuxCase(0.U,
 	  Array((selMux1W2 === 0.U) -> reg1W2,
 	        (selMux1W2 === 1.U) -> moduloSbox.io.msg_out,
 	        (selMux1W2 === 2.U) -> moduloARK,	
 	        (selMux1W2 === 3.U) -> Cat(reg1W2(31,24),moduloARK(23,16),reg1W0(15,8),reg1W1(7,0)),
 	        (selMux1W0 === 4.U) -> Cat(arkW2(31,24),arkW3(23,16),arkW0(15,8),arkW1(7,0)),
 	        (selMux1W2 === 5.U) -> auxiliary))
	val mux1W3 = MuxCase(0.U,
 	  Array((selMux1W3 === 0.U) -> reg1W3,
 	        (selMux1W3 === 1.U) -> moduloSbox.io.msg_out,
 	        (selMux1W3 === 2.U) -> moduloARK,	
 	        (selMux1W3 === 3.U) -> Cat(moduloARK(31,24),reg1W0(23,16),reg1W1(15,8),reg1W2(7,0)),
 	        (selMux1W0 === 4.U) -> Cat(arkW3(31,24),arkW0(23,16),arkW1(15,8),arkW2(7,0)),
 	        (selMux1W3 === 5.U) -> auxiliary))
	
	val start = ctrlGlobal(0) && ~RegNext(ctrlGlobal(0),false.B)
	
	when(busy){
		reg1K0 := mux1K0
		reg1K1 := mux1K1
		reg1K2 := mux1K2
		reg1K3 := mux1K3		
		reg1W0 := mux1W0
		reg1W1 := mux1W1
		reg1W2 := mux1W2
		reg1W3 := mux1W3
		ctrlGlobal := 0.U
	}
	
	regi(0) := Cat(0.U(30.W),~busy)
	
	switch (state) {
		is (standBy) {
		  when (start) {
		    state 		:= stage0
				ronda 		:= 0.U
				selMux1W0	:= 4.U
				selMux1W1	:= 4.U
				selMux1W2	:= 4.U
				selMux1W3	:= 4.U
				selMuxSbox	:= 4.U
				selMuxMixARK:= 0.U
				selKey		:= 1.U
			}.otherwise{
				state 	:= standBy
				ronda 		:= 0.U
				selMux1W0	:= 0.U
				selMux1W1	:= 0.U
				selMux1W2	:= 0.U
				selMux1W3	:= 0.U
				selMuxSbox	:= 4.U
				selMuxMixARK:= 0.U
				selKey		:= 0.U
			}
		}
		is (stage0) {
		  state 		:= stage1
			ronda 		:= ronda
			selMux1W0	:= 1.U
			selMux1W1	:= 0.U
			selMux1W2	:= 0.U
			selMux1W3	:= 0.U
			selMuxSbox	:= 0.U
			selMuxMixARK:= 0.U
			selKey		:= 0.U
		}
		is (stage1) {
			state 		:= stage2
			ronda 		:= ronda
			selMux1W0	:= 2.U
			selMux1W1	:= 1.U
			selMux1W2	:= 0.U
			selMux1W3	:= 0.U
			selMuxSbox	:= 1.U
			selMuxMixARK:= 0.U
			selKey		:= 0.U
		}
		is (stage2) {
			state 		:= stage3
			ronda 		:= ronda
			selMux1W0	:= 0.U
			selMux1W1	:= 2.U
			selMux1W2	:= 1.U
			selMux1W3	:= 0.U
			selMuxSbox	:= 2.U
			selMuxMixARK:= 1.U
			selKey		:= 0.U
		}
		is (stage3) {
			state 		:= stage4
			ronda 		:= ronda
			selMux1W0	:= 0.U
			selMux1W1	:= 0.U
			selMux1W2	:= 2.U
			selMux1W3	:= 1.U
			selMuxSbox	:= 3.U
			selMuxMixARK:= 2.U
			selKey		:= 0.U
		}
		is (stage4) {
		  state 		:= stage5
			ronda 		:= ronda + 1.U
			selMux1W0	:= 3.U
			selMux1W1	:= 3.U
			selMux1W2	:= 3.U
			selMux1W3	:= 3.U
			selMuxSbox	:= 4.U
			selMuxMixARK:= 3.U
			selKey		:= 1.U
		}
		is (stage5) {
			when (ronda <= 8.U) {
		    state 		:= stage1
		    ronda 		:= ronda
				selMux1W0	:= 1.U
				selMux1W1	:= 0.U
				selMux1W2	:= 0.U
				selMux1W3	:= 0.U
				selMuxSbox	:= 0.U
				selMuxMixARK:= 0.U
				selKey		:= 0.U
		  }.otherwise {
		    state 		:= stage6
				ronda 		:= 0.U
				selMux1W0	:= 5.U
				selMux1W1	:= 0.U
				selMux1W2	:= 0.U
				selMux1W3	:= 0.U
				selMuxSbox	:= 0.U
				selMuxMixARK:= 0.U
				selKey		:= 0.U
			}
		}
		is (stage6) {
		  state 		:= stage7
			ronda 		:= 0.U
			selMux1W0	:= 0.U
			selMux1W1	:= 5.U
			selMux1W2	:= 0.U
			selMux1W3	:= 0.U
			selMuxSbox	:= 1.U
			selMuxMixARK:= 1.U
			selKey		:= 0.U
		}
		is (stage7) {
			state 		:= stage8
			ronda 		:= 0.U
			selMux1W0	:= 0.U
			selMux1W1	:= 0.U
			selMux1W2	:= 5.U
			selMux1W3	:= 0.U
			selMuxSbox	:= 2.U
			selMuxMixARK:= 2.U
			selKey		:= 0.U
		}
		is (stage8) {
			state 		:= stage9
			ronda 		:= 0.U
			selMux1W0	:= 0.U
			selMux1W1	:= 0.U
			selMux1W2	:= 0.U
			selMux1W3	:= 5.U
			selMuxSbox	:= 3.U
			selMuxMixARK:= 3.U
			selKey		:= 0.U
		}
		is (stage9) {
			state 	:= standBy
			ronda 		:= 0.U
			selMux1W0	:= 0.U
			selMux1W1	:= 0.U
			selMux1W2	:= 0.U
			selMux1W3	:= 0.U
			selMuxSbox	:= 4.U
			selMuxMixARK:= 0.U
			selKey		:= 0.U
		} 
	}
}

//fossiAES.aesMain
object aesMain extends App {
	(new chisel3.stage.ChiselStage).emitVerilog(new aes(32,BigInt(0x20000080))  ,args)
}
