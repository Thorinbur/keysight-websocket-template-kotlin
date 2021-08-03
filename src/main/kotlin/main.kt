import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIOServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class CameraPosition(
    var x: Float,
    var y: Float,
    var z: Float,
    var tilt: Float,
    var rotate: Float,
    var roll: Float,
    var localRoll: Float,
    var fov: Float
)

fun main(){
    var client:SocketIOClient? = null

    val config = Configuration()
    config.hostname = "localhost";
    config.port = 3000;

    val server = SocketIOServer(config)

    server.addConnectListener{
        client = it
    }
    server.start()
    runBlocking {
        launch {
            val currentPosition = CameraPosition(
                x = 0f,
                y = -48.62f,
                z = 72.01f,
                tilt = -43.06f,
                rotate = 0f,
                roll = -38.03f,
                localRoll = 90f,
                fov = 65f
            )
            val lowerBound = -50f
            val upperBound = 50f
            var movementsSpeed = 0.5f

            while(true) {
                currentPosition.x += movementsSpeed
                val pastUpperBoundAndRising = movementsSpeed > 0 && currentPosition.x > upperBound
                val pastLowerBoundAndFalling = movementsSpeed < 0 && currentPosition.x < lowerBound
                if(pastUpperBoundAndRising || pastLowerBoundAndFalling){
                    movementsSpeed = 0f - movementsSpeed
                }
                client?.sendCameraEvent(currentPosition)

                delay(1000L / 60) // update position 60 times a second
            }
        }
    }
}

fun SocketIOClient.sendCameraEvent(pos:CameraPosition) =
    sendEvent(
        "camera",
        "camera:{\"x\":${pos.x},\"y\":${pos.y},\"z\":${pos.z},\"tilt\":${pos.tilt},\"rotate\":${pos.rotate},\"roll\":${pos.roll},\"localRoll\":${pos.localRoll},\"FOV\":${pos.fov}}"
    )
