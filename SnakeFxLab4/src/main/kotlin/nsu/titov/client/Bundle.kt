package nsu.titov.client

import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.control.ListView
import nsu.titov.proto.SnakeProto

data class Bundle(
    val canvas: Canvas,
    val hostNameLabel: Label,
    val fieldSizeLabel: Label,
    val foodRuleLabel: Label,
    val errorLabel: Label,

    val currentGameInfo: ListView<String>,
    val currentGameInfoList: ObservableList<String>,

    val availableServers: ListView<AnnounceItem>,
    val availableServersList: ObservableList<AnnounceItem>
)
