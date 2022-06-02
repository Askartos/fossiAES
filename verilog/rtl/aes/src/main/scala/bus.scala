/**********************************************************
*	Name:  				Whishbone peripheral interface 
*
*	Author: 			Hanssel Morales 2022
*																																							
* Abstract:	   Simple peripheral interface
***********************************************************/

package fossiAES

import chisel3._
import chisel3.util._
class whishbone_slave extends Bundle{

  val wbs_adr_i= Input(UInt(32.W))
  val wbs_sel_i= Input(UInt(4.W))
  val wbs_dat_i= Input(Bits(32.W))
  val wbs_stb_i= Input(Bool())
  val wbs_cyc_i= Input(Bool())
  val wbs_we_i = Input(Bool())

  val wbs_dat_o= Output(UInt(32.W))
  val wbs_ack_o= Output(Bool())
}

