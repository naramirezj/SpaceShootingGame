import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL //# GL# we need this for the constants declared ˙HUN˙ a constansok miatt kell
import kotlin.js.Date
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec1
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import vision.gears.webglmath.Mat4
import kotlin.math.*
import kotlin.random.Random


class Scene (
  val gl : WebGL2RenderingContext)  : UniformProvider("scene") {

  val vsTextured = Shader(gl, GL.VERTEX_SHADER, "textured-vs.glsl")
  val vsBackground = Shader(gl, GL.VERTEX_SHADER, "background-vs.glsl")  
  val fsTextured = Shader(gl, GL.FRAGMENT_SHADER, "textured-fs.glsl")
  val texturedProgram = Program(gl, vsTextured, fsTextured)
  val backgroundProgram = Program(gl, vsBackground, fsTextured)

  val gameOverMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/gameover.png"))
  }

  val youWinMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/youwin.png"))
  }

  val fighterMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/spaceship.png"))
  }
  val backgroundMaterial = Material(backgroundProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/galaxy.jpeg"))
  }

  val bouncerMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/station.png"))
  }

  val projectileMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/missile.png"))
  }

  val flameMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/flame.png"))
  }

  val ufoMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/ufo.png"))
  }

  val moonMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/moon.png"))
  }

  val jupiterMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/jupiter.png"))
  }

  val texturedQuadGeometry = TexturedQuadGeometry(gl)
  val backgroundMesh = Mesh(backgroundMaterial, texturedQuadGeometry)
  val fighterMesh = Mesh(fighterMaterial, texturedQuadGeometry)
  val bouncerMesh = Mesh(bouncerMaterial, texturedQuadGeometry)
  val missileMesh = Mesh(projectileMaterial, texturedQuadGeometry)
  val ufoMesh = Mesh(ufoMaterial, texturedQuadGeometry)
  val moonMesh = Mesh(moonMaterial, texturedQuadGeometry)
  val jupiterMesh = Mesh(jupiterMaterial, texturedQuadGeometry)
  val flameMesh = Mesh(flameMaterial, texturedQuadGeometry)
  val gameOverMesh = Mesh(gameOverMaterial, texturedQuadGeometry)
  val youWinMesh = Mesh(youWinMaterial, texturedQuadGeometry)

  val vsBoom = Shader(gl, GL.VERTEX_SHADER, "boom-vs.glsl")
  val fsBoom = Shader(gl, GL.FRAGMENT_SHADER, "boom-fs.glsl")  
  val programBoom = Program(gl, vsBoom, fsBoom, Program.PNT)
  
  val camera = OrthoCamera().apply{
    position.set(1f, 1f)
    windowSize.set(20f, 20f)
    updateViewProjMatrix()
  }

  var gameObjects = ArrayList<GameObject>()

  val bouncer = object : PhysicsGameObject(bouncerMesh){
    var thrust = 0.0f
  }.apply{
    var random_x =(Random.nextInt(-8, 9) * 1.0).toFloat()
    var random_y =(Random.nextInt(-8, 9) * 1.0).toFloat()
    position.set(random_x, random_y, 0.0f)
    torque -= 1
    invMass = 0.0f
    restitutionCoeff = 1.5f
    name = "bouncer"
  }

  val bouncer2 = object : PhysicsGameObject(bouncerMesh){
    var thrust = 0.0f
  }.apply{
    var random_x =(Random.nextInt(-12, 13) * 1.0).toFloat()
    var random_y =(Random.nextInt(-12, 13) * 1.0).toFloat()
    position.set(random_x, random_y, 0.0f)
    torque += 1f
    invMass = 0.0f
    restitutionCoeff = 1.5f
    name = "bouncer2"
  }

  

   val jupiter = object : PhysicsGameObject(jupiterMesh){
    var thrust = 0.0f
  }.apply{
    position.set(10f, 6f, 0.0f)
    torque += 0.3f
    invMass = 0.0f
    restitutionCoeff = 1.5f
    name = "jupiter"
    scale.set(5f, 5f)
  }

  val avatar = object : PhysicsGameObject(fighterMesh){
    var thrust = 0.0f

     init {
        position.set(2.0f, 2.0f, 0.0f) // Set the initial position to a valid value
        roll = 0.0f // Set the initial roll to a valid value
        name = "avatar"
    }
    override fun control (
      dt : Float,
      t : Float,
      keysPressed : Set<String>,
      gameObjects : List<GameObject>
      ) : Boolean {
        if("A" in keysPressed) 
        {
          torque = 2.0f
        }
        else if ("D" in keysPressed)
        {
          torque = -2.0f
        }
        else
        {
          torque = 0.0f
        }

        if("W" in keysPressed) 
        {
          thrust = 10.0f
          createFlame(position, roll)
        }
        else if("S" in keysPressed){
          thrust = -10.0f
        }
        else
        {
          thrust = 0.0f
        }

        if("SPACE" in keysPressed){
          createProjectile(position, torque, roll)
        }

        val ahead = Vec3 (cos (roll), sin (roll), 0.0f)
        force.set(ahead * thrust)

        return true
      }
  }

  init {
    gameObjects += GameObject(backgroundMesh)
    gameObjects += avatar
    gameObjects += bouncer
    avatar.roll = 1f
    createUfos()
    createMoons()
    gameObjects += bouncer2
    //gameObjects += moon
    gameObjects += jupiter
  }

  fun resize(canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)//#viewport# tell the rasterizer which part of the canvas to draw to ˙HUN˙ a raszterizáló ide rajzoljon
    camera.setAspectRatio(canvas.width.toFloat()/canvas.height)
  }

  var ufoCounter = -1

  fun youLose(avatarPosition: Vec3){
      gameObjects.clear()
      gameObjects += GameObject(backgroundMesh)
      gameObjects += GameObject(gameOverMesh).apply{
        position.set(avatarPosition)
        scale.set(2f,2f,2f)
      }
    }

     fun createMoons(){
        val moons = List(4) {
        val randomX = (Random.nextInt(-30, 30) * 1.0).toFloat()  
        val randomY = (Random.nextInt(-20, 20) * 1.0).toFloat()  
        //val randomRoll = kotlin.math.toRadians((Random.nextInt(0, 361) * 1.0).toFloat())  
        object : PhysicsGameObject(moonMesh){
        var thrust = 0.0f
      }.apply{
        position.set(randomX, randomY, 0.0f)
        torque += 0.2f
        invMass = 0.0f
        restitutionCoeff = 1.5f
        name = "moon"
        scale.set(4f, 4f)
      }
    }

    gameObjects.addAll(moons)
  }

    fun createUfos(){
        val ufos = List(5) {
        val randomX = (Random.nextInt(-20, 20) * 1.0).toFloat()  
        val randomY = (Random.nextInt(-20, 20) * 1.0).toFloat()  
        //val randomRoll = kotlin.math.toRadians((Random.nextInt(0, 361) * 1.0).toFloat())  

        object : PhysicsGameObject(ufoMesh) {
          val thrust = 2.0f
          
          override fun collisionMove (
          dt : Float,
          t : Float,
          keysPressed : Set<String>,
          gameObjects : List<GameObject>
          ) : Boolean {
            gameObjects.forEach {
            if (it is PhysicsGameObject) {
              if (it != this && it.name == "avatar" && name == "alien") {
                val diff = position - it.position
                val dist = diff.length()
                if (dist < radius + it.radius) {
                 youLose(it.position)
                } 
              }
            }
          }
          return true
          }

          }.apply {
            position.set(randomX, randomY, 0.0f)
            roll = Random.nextFloat()
            name = "alien"
            force.set(thrust, thrust, 0.0f)
        }
    }

    gameObjects.addAll(ufos)
    ufoCounter = 5
  }

  fun updateUfosPosition(playerPosition: Vec3) {
    gameObjects.filterIsInstance<PhysicsGameObject>().forEach { gameObject ->
      if (gameObject is PhysicsGameObject) {
        if (gameObject.name == "alien") {
            val directionToPlayer = playerPosition - gameObject.position
            val normalizedDirection = directionToPlayer.normalize()
            gameObject.force.set(normalizedDirection * 3.0f)
        }
      }
    }
}

  fun removeUfos(id: String){
        // Iterate in reverse to avoid issues when removing elements during iteration
        ufoCounter = ufoCounter - 1
        for (i in gameObjects.size - 1 downTo 0) {
            val gameObject = gameObjects[i]
            if (gameObject is PhysicsGameObject){
              if(gameObject.name == "alien" && gameObject.id == id) {
                    gameObjects.removeAt(i)
                }
              }
            }
        }

   fun createFlame(avatarPosition: Vec3, avatarRoll: Float){
        val flameOffset = Vec3(-1.5f, 0.1f, 0.0f)
        val flame = object : PhysicsGameObject(flameMesh){}.apply{
          val offset = Vec3(
                  cos(avatarRoll) * flameOffset.x - sin(avatarRoll) * flameOffset.y,
                  sin(avatarRoll) * flameOffset.x + cos(avatarRoll) * flameOffset.y,
                  flameOffset.z
              )
          position.set(avatarPosition.clone() + offset)
          roll = avatarRoll
          scale.set(1f, 1f)
          name = "flame"
          creationTime += (Date().getTime() / 1000.0f).toFloat()
        }
      gameObjects += flame
      //lastFlameTime = currentTime.toFloat()
   }


  var lastProjectileTime: Float = 0f
  val projectileCooldown: Float = 2f

  fun createProjectile(avatarPosition: Vec3, avatarTorque: Float, avatarRoll: Float){
    val currentTime = Date().getTime() / 1000.0f
    val cooldownElapsed = currentTime - lastProjectileTime
    if (cooldownElapsed >= projectileCooldown) {
    val projectile = object : PhysicsGameObject(missileMesh){
      val thrust = 20.0f
      val ahead = Vec3 (cos (avatarRoll), sin (avatarRoll), 0.0f)
      override fun collisionMove (
      dt : Float,
      t : Float,
      keysPressed : Set<String>,
      gameObjects : List<GameObject>
      ) : Boolean {
        gameObjects.forEach {
        if (it is PhysicsGameObject) {
          if (it != this && it.name != "avatar" && it.name != "projectile" && it.name != "flame") {
            val diff = position - it.position
            val dist = diff.length()
            if (dist < radius + it.radius) {
              val collisionNormal = diff.normalize()
              position += collisionNormal * invMass / (invMass + it.invMass) * dist * 0.01f
              //it.position += collisionNormal * it.invMass / (invMass + it.invMass) * dist * -0.01f
              createExplosion(position, id)
              if (it.name == "alien") {
                  removeUfos(it.id)
                }
              } 
          }
        }
      }
      return true
      } }.apply{
        roll = avatarRoll
        position.set(avatarPosition)
        force.set(ahead * thrust)
        name = "projectile"
      }
    gameObjects += projectile
    lastProjectileTime = currentTime.toFloat()
  }
}

  
  val boomMaterial = Material(programBoom).apply{
    uniforms["colorTexture"]?.set(Texture2D (gl, "media/boom.png"))
  }

  val boomMesh = Mesh(boomMaterial, texturedQuadGeometry)

  fun createExplosion(projectilePosition: Vec3, projectileId: String){
    val index = gameObjects.indexOfFirst {
        it is PhysicsGameObject && it.id == projectileId
    }

    val boom = object : GameObject(boomMesh){
      override fun move(
            dt : Float,
            t : Float,
            keysPressed : Set<String>,
            gameObjects : List<GameObject>
            ) : Boolean {
            dtCounter += dt
            if (dtCounter >  0.01f){
              dtCounter = 0.0f
              shiftCounter = shiftCounter + 1
            }
            shift.set (Vec2 ((shiftCounter % 6) * (1.0f / 6.0f), (shiftCounter / 6) * (1.0f / 6.0f)))
            return true
          }
      }.apply{
        position.set(projectilePosition)
        scale.set(1f, 1f)
        name = "boom"
      }
    if (index != -1) {
        gameObjects[index] = boom
        gameObjects[index].creationTime += (Date().getTime() / 1000.0f).toFloat()
    }
}


   val disappearTime: Float = 1f
     fun removeExpiredExplosions() {
        val currentTime = Date().getTime() / 1000.0f

        for (i in gameObjects.size - 1 downTo 0) {
            val gameObject = gameObjects[i]
            if (gameObject is GameObject && gameObject.name == "boom") {
                val elapsedTime = currentTime - gameObject.creationTime
                if (elapsedTime >= disappearTime) {
                    gameObjects.removeAt(i)
                }
            }
        }
    }

    val disappearTimeFlame: Float = 0.1f
     fun removeExpiredFlames() {
        val currentTime = Date().getTime() / 1000.0f

        for (i in gameObjects.size - 1 downTo 0) {
            val gameObject = gameObjects[i]
            if (gameObject is GameObject && gameObject.name == "flame") {
                val elapsedTime = currentTime - gameObject.creationTime
                if (elapsedTime >= disappearTimeFlame) {
                    gameObjects.removeAt(i)
                }
            }
        }
    }

    fun youWin(avatarPosition: Vec3){
      gameObjects.clear()
      gameObjects += GameObject(backgroundMesh)
      gameObjects += GameObject(youWinMesh).apply{
        position.set(avatarPosition)
        scale.set(2f,2f,2f)
      }
    }

  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  @Suppress("UNUSED_PARAMETER")
  fun update(keysPressed : Set<String>) {
    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f

    //TODO: set property time (reflecting uniform scene.time) 
    timeAtLastFrame = timeAtThisFrame
    
    camera.position.set(avatar.position)
    camera.updateViewProjMatrix()

    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)//## red, green, blue, alpha in [0, 1]
    gl.clearDepth(1.0f)//## will be useful in 3D ˙HUN˙ 3D-ben lesz hasznos
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)//#or# bitwise OR of flags

    gl.enable(GL.BLEND)
    gl.blendFunc(
      GL.SRC_ALPHA,
      GL.ONE_MINUS_SRC_ALPHA)
    console.log(ufoCounter)

    if(ufoCounter == -6){
      //camera.position.set(0f,0f,0f)
      youWin(avatar.position)
    }

    gameObjects.forEach{
      it.move(dt, t, keysPressed, gameObjects)
    }

    updateUfosPosition(avatar.position)

    gameObjects.forEach{
      it.update()
    }
    gameObjects.forEach{
      it.draw(this, camera)
    }
    removeExpiredExplosions()
    removeExpiredFlames()
  
  }
}
