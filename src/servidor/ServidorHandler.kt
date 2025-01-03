package servidor

import estaticos.AtlantaMain
import lombok.extern.slf4j.Slf4j
import org.apache.mina.core.service.IoHandler
import org.apache.mina.core.session.IdleStatus
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.FilterEvent
import org.apache.mina.filter.codec.RecoverableProtocolDecoderException
import servidor.filter.PacketFilter
import variables.personaje.Personaje

@Slf4j
class ServidorHandler : IoHandler {
    @Throws(Exception::class)
    override fun sessionCreated(arg0: IoSession) {
        if (!filter.authorizes(
                        arg0.remoteAddress.toString().substring(1).split(":".toRegex()).toTypedArray()[0]
                )
        ) {
            arg0.closeNow()
        } else {
            arg0.attachment = ServidorSocket(arg0)
        }
    }

    @Throws(Exception::class)
    override fun messageReceived(arg0: IoSession, arg1: Any) {
        val client = arg0.attachment as ServidorSocket?
        val packet = arg1 as String?
        val s = packet?.split("\n".toRegex())?.toTypedArray() ?: arrayOf()
        if (client == null) {
            arg0.closeNow()
            return
        }
        if (AtlantaMain.TOKEN == "Testing") {
            if (ServidorServer.nroJugadoresLinea() > 25) {
                arg0.closeNow()
            }
        }
        try {
            s.forEach { str ->
                if (str.isNotBlank()) {
                    if (AtlantaMain.MOSTRAR_RECIBIDOS) {
                        println("<<RECIBIR PERSONAJE ${client.personaje?.nombre}:  $str")
                    }
                    client.logger.debug("<=== Recibido: $str")
                    client.rastrear(str)
                    client.registrar("===>> $str")
                    val personaje = client.personaje
                    if (personaje != null) {
                        val multi: Personaje? = personaje.Multi
                        val socketm: ServidorSocket? = multi?.cuenta?.socket
                        if (socketm != null) {
                            if (multi != personaje) {
                                multi.cuenta.socket?.threadPackets(str)
                            } else {
                                client.threadPackets(str)
                            }
                        } else {
                            if (multi != null && personaje.pelea != null) client.personaje = multi
                            client.threadPackets(str)
                        }
                    } else {
                        client.threadPackets(str)
                    }
//                    ENVIAR_BN_NADA(personaje)
                    client.logger.trace(" <-- $str")
                }
            }
        } catch (e: Exception) {
        }
    }

    @Throws(Exception::class)
    override fun sessionClosed(arg0: IoSession) {
        val client = arg0.attachment as ServidorSocket?
        client?.disconnect()
        arg0.attachment = null
        arg0.closeNow()
    }

    @Throws(Exception::class)
    override fun exceptionCaught(arg0: IoSession, arg1: Throwable) {
        if (arg1.message != null && (arg1 is RecoverableProtocolDecoderException || arg1.message!!.startsWith("Une connexion ") ||
                        arg1.message!!.startsWith("Connection reset by peer") || arg1.message!!.startsWith("Connection timed out"))
        ) return
        arg1.printStackTrace()
        val client = arg0.attachment as ServidorSocket?
        client?.logger?.warn("Exception connexion client : " + arg1.message)
        kick(arg0)
        arg0.attachment = null
        arg0.closeNow()
    }

    @Throws(Exception::class)
    override fun messageSent(arg0: IoSession, arg1: Any) {
        val client = arg0.attachment as ServidorSocket?
        if (client != null) {
            val packet = arg1 as String
            client.logger.trace(" --> $packet")
            client.logger.debug("===> Enviado: $packet")
        } else {
            arg0.attachment = null
            arg0.closeNow()
        }
    }

    @Throws(Exception::class)
    override fun inputClosed(ioSession: IoSession) {
        val Session = ioSession.attachment as ServidorSocket?
        Session?.disconnect()
        ioSession.attachment = null
        ioSession.closeNow()
    }

    @Throws(Exception::class)
    override fun event(ioSession: IoSession, filterEvent: FilterEvent) {
    }

    @Throws(Exception::class)
    override fun sessionIdle(arg0: IoSession, arg1: IdleStatus) {
    }

    @Throws(Exception::class)
    override fun sessionOpened(arg0: IoSession) {
//        log.info("Session " + arg0.getId() + " opened");
    }

    private fun kick(arg0: IoSession) {
        val client = arg0.attachment as ServidorSocket?
        try {
            client?.kick()
        } catch (e: Exception) {
        }
        arg0.attachment = null
        arg0.closeNow()
    }

    companion object {
        val filter = PacketFilter().activeSafeMode()
    }
}