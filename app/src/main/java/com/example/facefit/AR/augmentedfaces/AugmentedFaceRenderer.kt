package com.example.facefit.AR.augmentedfaces

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.google.ar.core.AugmentedFace
import com.google.ar.core.examples.kotlin.common.rendering.ShaderUtil.loadGLShader
import java.io.IOException

/** Renders an AugmentedFace on screen in OpenGL. */
class AugmentedFaceRenderer {

    // Uniform locations for shader variables
    private var modelViewUniform = 0
    private var modelViewProjectionUniform = 0
    private var textureUniform = 0
    private var lightingParametersUniform = 0
    private var materialParametersUniform = 0
    private var colorCorrectionParameterUniform = 0
    private var tintColorUniform = 0

    // Attribute locations for vertex data
    private var attriVertices = 0
    private var attriUvs = 0
    private var attriNormals = 0

    // Default material properties for lighting
    private var ambient = 0.3f
    private var diffuse = 1.0f
    private var specular = 1.0f
    private var specularPower = 6.0f

    // Texture ID for the face texture
    private val textureId = IntArray(1)

    // Matrices for transformations
    private val modelViewProjectionMat = FloatArray(16)
    private val modelViewMat = FloatArray(16)
    private val viewLightDirection = FloatArray(4)

    // OpenGL program ID
    private var program = 0

    companion object {
        private const val TAG = "AugmentedFaceRenderer"
        private val lightDirection = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f) // Light direction vector
        private const val VERTEX_SHADER_NAME = "shaders/object.vert" // Vertex shader file name
        private const val FRAGMENT_SHADER_NAME = "shaders/object.frag" // Fragment shader file name

        // Normalize a 3D vector
        private fun normalizeVec3(v: FloatArray) {
            val reciprocalLength = 1.0f / Math.sqrt((v[0] * v[0] + v[1] * v[1] + v[2] * v[2]).toDouble()).toFloat()
            v[0] *= reciprocalLength
            v[1] *= reciprocalLength
            v[2] *= reciprocalLength
        }

        // Load a texture from the assets folder
        @Throws(IOException::class)
        private fun loadTexture(context: Context, textureId: IntArray, filename: String) {
            val textureBitmap = BitmapFactory.decodeStream(context.assets.open(filename))
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0)
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            textureBitmap.recycle()
        }
    }

    // Create the OpenGL program and load the texture
    @Throws(IOException::class)
    fun createOnGlThread(context: Context, diffuseTextureAssetName: String) {
        val vertexShader = loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME)
        val fragmentShader = loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        // Get uniform locations from the shader program
        modelViewProjectionUniform = GLES20.glGetUniformLocation(program, "u_ModelViewProjection")
        modelViewUniform = GLES20.glGetUniformLocation(program, "u_ModelView")
        textureUniform = GLES20.glGetUniformLocation(program, "u_Texture")
        lightingParametersUniform = GLES20.glGetUniformLocation(program, "u_LightningParameters")
        materialParametersUniform = GLES20.glGetUniformLocation(program, "u_MaterialParameters")
        colorCorrectionParameterUniform = GLES20.glGetUniformLocation(program, "u_ColorCorrectionParameters")
        tintColorUniform = GLES20.glGetUniformLocation(program, "u_TintColor")

        // Get attribute locations from the shader program
        attriVertices = GLES20.glGetAttribLocation(program, "a_Position")
        attriUvs = GLES20.glGetAttribLocation(program, "a_TexCoord")
        attriNormals = GLES20.glGetAttribLocation(program, "a_Normal")

        // Load the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glGenTextures(1, textureId, 0)
        loadTexture(context, textureId, diffuseTextureAssetName)
    }

    // Draw the augmented face
    fun draw(
        projmtx: FloatArray,
        viewmtx: FloatArray,
        modelmtx: FloatArray,
        colorCorrectionRgba: FloatArray,
        face: AugmentedFace
    ) {
        val vertices = face.meshVertices
        val normals = face.meshNormals
        val textureCoords = face.meshTextureCoordinates
        val triangleIndices = face.meshTriangleIndices

        GLES20.glUseProgram(program)
        GLES20.glDepthMask(false)

        // Calculate the model-view-projection matrix
        val modelViewProjectionMatTemp = FloatArray(16)
        Matrix.multiplyMM(modelViewProjectionMatTemp, 0, projmtx, 0, viewmtx, 0)
        Matrix.multiplyMM(modelViewProjectionMat, 0, modelViewProjectionMatTemp, 0, modelmtx, 0)
        Matrix.multiplyMM(modelViewMat, 0, viewmtx, 0, modelmtx, 0)

        // Calculate the light direction in the view space
        Matrix.multiplyMV(viewLightDirection, 0, modelViewMat, 0, lightDirection, 0)
        normalizeVec3(viewLightDirection)

        // Set the shader uniforms
        GLES20.glUniform4f(
            lightingParametersUniform,
            viewLightDirection[0],
            viewLightDirection[1],
            viewLightDirection[2],
            1f
        )
        GLES20.glUniform4fv(colorCorrectionParameterUniform, 1, colorCorrectionRgba, 0)
        GLES20.glUniform4f(materialParametersUniform, ambient, diffuse, specular, specularPower)
        GLES20.glUniformMatrix4fv(modelViewUniform, 1, false, modelViewMat, 0)
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, modelViewProjectionMat, 0)

        // Set the vertex attributes
        GLES20.glEnableVertexAttribArray(attriVertices)
        GLES20.glVertexAttribPointer(attriVertices, 3, GLES20.GL_FLOAT, false, 0, vertices)

        GLES20.glEnableVertexAttribArray(attriNormals)
        GLES20.glVertexAttribPointer(attriNormals, 3, GLES20.GL_FLOAT, false, 0, normals)

        GLES20.glEnableVertexAttribArray(attriUvs)
        GLES20.glVertexAttribPointer(attriUvs, 2, GLES20.GL_FLOAT, false, 0, textureCoords)

        // Bind the texture and set the tint color
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(textureUniform, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
        GLES20.glUniform4f(tintColorUniform, 1f, 1f, 1f, 1f) // Set tint color to white
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Draw the face mesh
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            triangleIndices.limit(),
            GLES20.GL_UNSIGNED_SHORT,
            triangleIndices
        )

        GLES20.glUseProgram(0)
        GLES20.glDepthMask(true)
    }

    // Set the material properties for lighting
    fun setMaterialProperties(ambient: Float, diffuse: Float, specular: Float, specularPower: Float) {
        this.ambient = ambient
        this.diffuse = diffuse
        this.specular = specular
        this.specularPower = specularPower
    }
}