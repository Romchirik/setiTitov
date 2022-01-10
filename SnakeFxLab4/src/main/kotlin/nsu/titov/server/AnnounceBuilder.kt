package nsu.titov.server

import nsu.titov.proto.SnakeProto

class AnnounceBuilder private constructor() {
    private var serverConfig: ServerConfig? = null
    private var players: Map<Int, ServerPlayerInfo> = HashMap()
    private var canJoin = true

    fun addPlayers(players: Map<Int, ServerPlayerInfo>): AnnounceBuilder {
        this.players = players
        return this
    }

    fun setJoin(value: Boolean): AnnounceBuilder {
        this.canJoin = value
        return this
    }

    fun setServerConfig(serverConfig: ServerConfig): AnnounceBuilder {
        this.serverConfig = serverConfig
        return this
    }

    fun build(): SnakeProto.GameMessage.AnnouncementMsg {
        assert(serverConfig != null) { "Set server config before building message" }

        val config = SnakeProto.GameConfig.newBuilder()
            .setWidth(serverConfig!!.playfieldWidth)
            .setHeight(serverConfig!!.playfieldHeight)
            .build()

        val playersTmp = SnakeProto.GamePlayers.newBuilder()
        this.players.forEach { (_, player) ->
            playersTmp.addPlayers(player.toProto())
        }

        return SnakeProto.GameMessage.AnnouncementMsg.newBuilder()
            .setPlayers(playersTmp)
            .setConfig(config)
            .setCanJoin(canJoin).build()
    }

    companion object {
        fun getBuilder(): AnnounceBuilder {
            return AnnounceBuilder()
        }
    }
}