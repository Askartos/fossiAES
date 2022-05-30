/**********************************************************
*	Name:  				BUS
*
*	Author: 			Hanssel Norato-Sep 17, 2019.
*
*	Modified by: 		Oscar Pardo-27/08/2020.												
*																													
* Abstract:		An AES accelerator
***********************************************************/

package fossiAES

import chisel3._
import chisel3.util._
object BusConsts{
//
  def AddrBits = 32
  def DataBits = 32
  def BURST    = 3
  def PROT     = 4
  def SIZE     = 3
  def TRANS    = 2
  
  // Operation HTRANS modes 
  def IDLE     = 0
  def BUSY     = 1  
  def NONSEQ   = 2  
  def SEQ      = 3  
  
  
  // Protection control HPROT
  def Opcode_fetch      = 0 //"b0"
  def Data_access       = 1
  def User_access       = 0
  def Privileged_access = 2
  def Non_bufferable    = 0
  def bufferable        = 4
  def Non_cacheable     = 0 
  def cacheable         = 16 
  
}
class Condor_SlaveIO extends Bundle{

  val caddr    = Input(UInt(BusConsts.AddrBits.W))
  val cop      = Input(UInt(BusConsts.TRANS.W))
  val cwrite   = Input(Bool())
  val creq     = Input(Bool())
  val cvalid   = Input(Bool())
  val cburst   = Input(UInt(BusConsts.BURST.W))
  val csel     = Input(Bool())
  val csize    = Input(UInt(BusConsts.SIZE.W))  
  val cprot    = Input(UInt(BusConsts.PROT.W))
  val cready   = Input(Bool())
  val cwdata   = Input(Bits(BusConsts.DataBits.W))

  val crdata     = Output(Bits(BusConsts.DataBits.W))
  val creadyout  = Output(Bool())
  val cresp      = Output(Bool())
}

