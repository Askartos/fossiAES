/**********************************************************
*	Name:  				MizColumns
*
*	Author: 			Oscar Pardo-2020.
*
*	Modified by: 		Oscar Pardo- 07/2020.											
*																													
* Abstract:				The MixColumns operation for AES
***********************************************************/

package fossiAES

import chisel3._
import chisel3.util._

class ports_mix extends Bundle {
	val msg 	= Input(UInt(32.W))
	val msg_out	= Output(UInt(32.W))
}

class mix() extends Module {
	val io = IO(new ports_mix)

	val b0 = Wire(UInt(8.W))
	val b1 = Wire(UInt(8.W))
	val b2 = Wire(UInt(8.W))
	val b3 = Wire(UInt(8.W))

	//(2,3,1,1)
	b0 := Mux( io.msg(31) , Cat(io.msg(30,24),0.U)^27.U , Cat(io.msg(30,24),0.U) ) ^ Mux( io.msg(23) , (Cat(io.msg(22,16),0.U)^27.U)^io.msg(23,16) , Cat(io.msg(22,16),0.U)^io.msg(23,16) ) ^ io.msg(15,8) ^ io.msg(7,0)
	//(1,2,3,1)
	b1 := io.msg(31,24) ^ Mux( io.msg(23) , Cat(io.msg(22,16),0.U)^27.U , Cat(io.msg(22,16),0.U) ) ^ Mux( io.msg(15) , (Cat(io.msg(14,8),0.U)^27.U)^io.msg(15,8) , Cat(io.msg(14,8),0.U)^io.msg(15,8) ) ^ io.msg(7,0)
	//(1,1,2,3)
	b2 := io.msg(31,24) ^ io.msg(23,16) ^ Mux( io.msg(15) , Cat(io.msg(14,8),0.U)^27.U , Cat(io.msg(14,8),0.U) ) ^ Mux( io.msg(7) , (Cat(io.msg(6,0),0.U)^27.U)^io.msg(7,0) , Cat(io.msg(6,0),0.U)^io.msg(7,0) )
	//(3,1,1,2)
	b3 := Mux( io.msg(31) , (Cat(io.msg(30,24),0.U)^27.U)^io.msg(31,24) , Cat(io.msg(30,24),0.U)^io.msg(31,24) ) ^ io.msg(23,16) ^ io.msg(15,8) ^ Mux( io.msg(7) , Cat(io.msg(6,0),0.U)^27.U , Cat(io.msg(6,0),0.U) )

	io.msg_out := Cat(b0,b1,b2,b3)
}
