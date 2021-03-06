
package com.badlogic.gdx.backends.jglfw;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.jglfw.gl.GL;
import com.badlogic.jglfw.utils.Memory;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class JglfwGL10 implements GL10 {
	private IntBuffer tempInt = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
	private FloatBuffer tempFloat = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

	private IntBuffer toBuffer (int n, int[] src, int offset) {
		if (tempInt.capacity() < n)
			tempInt = ByteBuffer.allocateDirect(n * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		else
			tempInt.clear();
		tempInt.put(src, offset, n);
		tempInt.flip();
		return tempInt;
	}

	private IntBuffer toBuffer (int[] src, int offset) {
		int n = src.length - offset;
		if (tempInt.capacity() < n)
			tempInt = ByteBuffer.allocateDirect(n * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		else
			tempInt.clear();
		tempInt.put(src, offset, n);
		tempInt.flip();
		return tempInt;
	}

	private FloatBuffer toBuffer (float[] src, int offset) {
		int n = src.length - offset;
		if (tempFloat.capacity() < n)
			tempFloat = ByteBuffer.allocateDirect(n * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		else
			tempFloat.clear();
		tempFloat.put(src, offset, src.length - offset);
		tempFloat.flip();
		return tempFloat;
	}

	public void glActiveTexture (int texture) {
		GL.glActiveTexture(texture);
	}

	public void glBindTexture (int target, int texture) {
		GL.glBindTextureEXT(target, texture);
	}

	public void glBlendFunc (int sfactor, int dfactor) {
		GL.glBlendFunc(sfactor, dfactor);
	}

	public void glClear (int mask) {
		GL.glClear(mask);
	}

	public void glClearColor (float red, float green, float blue, float alpha) {
		GL.glClearColor(red, green, blue, alpha);
	}

	public void glClearDepthf (float depth) {
		GL.glClearDepthf(depth);
	}

	public void glClearStencil (int s) {
		GL.glClearStencil(s);
	}

	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		GL.glColorMask(red, green, blue, alpha);
	}

	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		GL.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data, Memory.getPosition(data));
	}

	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		GL.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data,
			Memory.getPosition(data));
	}

	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GL.glCopyTexImage2DEXT(target, level, internalformat, x, y, width, height, border);
	}

	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GL.glCopyTexSubImage2DEXT(target, level, xoffset, yoffset, x, y, width, height);
	}

	public void glCullFace (int mode) {
		GL.glCullFace(mode);
	}

	public void glDeleteTextures (int n, IntBuffer textures) {
		GL.glDeleteTexturesEXT(n, textures, Memory.getPosition(textures));
	}

	public void glDepthFunc (int func) {
		GL.glDepthFunc(func);
	}

	public void glDepthMask (boolean flag) {
		GL.glDepthMask(flag);
	}

	public void glDepthRangef (float zNear, float zFar) {
		GL.glDepthRangef(zNear, zFar);
	}

	public void glDisable (int cap) {
		GL.glDisable(cap);
	}

	public void glDrawArrays (int mode, int first, int count) {
		GL.glDrawArraysEXT(mode, first, count);
	}

	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		GL.glDrawElements(mode, count, type, indices, Memory.getPosition(indices));
	}

	public void glEnable (int cap) {
		GL.glEnable(cap);
	}

	public void glFinish () {
		GL.glFinish();
	}

	public void glFlush () {
		GL.glFlush();
	}

	public void glFrontFace (int mode) {
		GL.glFrontFace(mode);
	}

	public void glGenTextures (int n, IntBuffer textures) {
		GL.glGenTexturesEXT(n, textures, Memory.getPosition(textures));
	}

	public int glGetError () {
		return GL.glGetError();
	}

	public void glGetIntegerv (int pname, IntBuffer params) {
		GL.glGetIntegerv(pname, params, Memory.getPosition(params));
	}

	public String glGetString (int name) {
		return GL.glGetString(name);
	}

	public void glHint (int target, int mode) {
		GL.glHint(target, mode);
	}

	public void glLineWidth (float width) {
		GL.glLineWidth(width);
	}

	public void glPixelStorei (int pname, int param) {
		GL.glPixelStorei(pname, param);
	}

	public void glPolygonOffset (float factor, float units) {
		GL.glPolygonOffsetEXT(factor, units);
	}

	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		GL.glReadPixels(x, y, width, height, format, type, pixels, Memory.getPosition(pixels));
	}

	public void glScissor (int x, int y, int width, int height) {
		GL.glScissor(x, y, width, height);
	}

	public void glStencilFunc (int func, int ref, int mask) {
		GL.glStencilFunc(func, ref, mask);
	}

	public void glStencilMask (int mask) {
		GL.glStencilMask(mask);
	}

	public void glStencilOp (int fail, int zfail, int zpass) {
		GL.glStencilOp(fail, zfail, zpass);
	}

	public void glTexImage2D (int target, int level, int internalFormat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		GL.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels, Memory.getPosition(pixels));
	}

	public void glTexParameterf (int target, int pname, float param) {
		GL.glTexParameterf(target, pname, param);
	}

	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		GL.glTexSubImage2DEXT(target, level, xoffset, yoffset, width, height, format, type, pixels, Memory.getPosition(pixels));
	}

	public void glViewport (int x, int y, int width, int height) {
		GL.glViewport(x, y, width, height);
	}

	public void glAlphaFunc (int func, float ref) {
		GL.glAlphaFunc(func, ref);
	}

	public void glClientActiveTexture (int texture) {
		GL.glClientActiveTexture(texture);
	}

	public void glColor4f (float red, float green, float blue, float alpha) {
		GL.glColor4f(red, green, blue, alpha);
	}

	public void glColorPointer (int size, int type, int stride, Buffer pointer) {
		GL.glColorPointer(size, type, stride, pointer, Memory.getPosition(pointer));
	}

	public void glDeleteTextures (int n, int[] textures, int offset) {
		GL.glDeleteTextures(n, toBuffer(textures, offset), 0);
	}

	public void glDisableClientState (int array) {
		GL.glDisableClientState(array);
	}

	public void glEnableClientState (int array) {
		GL.glEnableClientState(array);
	}

	public void glFogf (int pname, float param) {
		GL.glFogf(pname, param);
	}

	public void glFogfv (int pname, float[] params, int offset) {
		GL.glFogfv(pname, toBuffer(params, offset), 0);
	}

	public void glFogfv (int pname, FloatBuffer params) {
		GL.glFogfv(pname, params, Memory.getPosition(params));
	}

	public void glFrustumf (float left, float right, float bottom, float top, float zNear, float zFar) {
		GL.glFrustum(left, right, bottom, top, zNear, zFar);
	}

	public void glGenTextures (int n, int[] textures, int offset) {
		GL.glGenTexturesEXT(n, toBuffer(textures, offset), 0);
	}

	public void glGetIntegerv (int pname, int[] params, int offset) {
		GL.glGetIntegerv(pname, toBuffer(params, offset), 0);
	}

	public void glLightModelf (int pname, float param) {
		GL.glLightModelf(pname, param);
	}

	public void glLightModelfv (int pname, float[] params, int offset) {
		GL.glLightModelfv(pname, toBuffer(params, offset), 0);
	}

	public void glLightModelfv (int pname, FloatBuffer params) {
		GL.glLightModelfv(pname, params, Memory.getPosition(params));
	}

	public void glLightf (int light, int pname, float param) {
		GL.glLightf(light, pname, param);
	}

	public void glLightfv (int light, int pname, float[] params, int offset) {
		GL.glLightfv(light, pname, toBuffer(params, offset), 0);
	}

	public void glLightfv (int light, int pname, FloatBuffer params) {
		GL.glLightfv(light, pname, params, Memory.getPosition(params));
	}

	public void glLoadIdentity () {
		GL.glLoadIdentity();
	}

	public void glLoadMatrixf (float[] m, int offset) {
		GL.glLoadMatrixf(toBuffer(m, offset), 0);
	}

	public void glLoadMatrixf (FloatBuffer m) {
		GL.glLoadMatrixf(m, Memory.getPosition(m));
	}

	public void glLogicOp (int opcode) {
		GL.glLogicOp(opcode);
	}

	public void glMaterialf (int face, int pname, float param) {
		GL.glMaterialf(face, pname, param);
	}

	public void glMaterialfv (int face, int pname, float[] params, int offset) {
		GL.glMaterialfv(face, pname, toBuffer(params, offset), 0);
	}

	public void glMaterialfv (int face, int pname, FloatBuffer params) {
		GL.glMaterialfv(face, pname, params, Memory.getPosition(params));
	}

	public void glMatrixMode (int mode) {
		GL.glMatrixMode(mode);
	}

	public void glMultMatrixf (float[] m, int offset) {
		GL.glMultMatrixf(toBuffer(m, offset), 0);
	}

	public void glMultMatrixf (FloatBuffer m) {
		GL.glMultMatrixf(m, Memory.getPosition(m));
	}

	public void glMultiTexCoord4f (int target, float s, float t, float r, float q) {
		GL.glMultiTexCoord4f(target, s, t, r, q);
	}

	public void glNormal3f (float nx, float ny, float nz) {
		GL.glNormal3f(nx, ny, nz);
	}

	public void glNormalPointer (int type, int stride, Buffer pointer) {
		GL.glNormalPointer(type, stride, pointer, Memory.getPosition(pointer));
	}

	public void glOrthof (float left, float right, float bottom, float top, float zNear, float zFar) {
		GL.glOrtho(left, right, bottom, top, zNear, zFar);
	}

	public void glPointSize (float size) {
		GL.glPointSize(size);
	}

	public void glPopMatrix () {
		GL.glPopMatrix();
	}

	public void glPushMatrix () {
		GL.glPushMatrix();
	}

	public void glRotatef (float angle, float x, float y, float z) {
		GL.glRotatef(angle, x, y, z);
	}

	public void glSampleCoverage (float value, boolean invert) {
		GL.glSampleCoverage(value, invert);
	}

	public void glScalef (float x, float y, float z) {
		GL.glScalef(x, y, z);
	}

	public void glShadeModel (int mode) {
		GL.glShadeModel(mode);
	}

	public void glTexCoordPointer (int size, int type, int stride, Buffer pointer) {
		GL.glTexCoordPointer(size, type, stride, pointer, Memory.getPosition(pointer));
	}

	public void glTexEnvf (int target, int pname, float param) {
		GL.glTexEnvf(target, pname, param);
	}

	public void glTexEnvfv (int target, int pname, float[] params, int offset) {
		GL.glTexEnvfv(target, pname, toBuffer(params, offset), 0);
	}

	public void glTexEnvfv (int target, int pname, FloatBuffer params) {
		GL.glTexEnvfv(target, pname, params, Memory.getPosition(params));
	}

	public void glTranslatef (float x, float y, float z) {
		GL.glTranslatef(x, y, z);
	}

	public void glVertexPointer (int size, int type, int stride, Buffer buffer) {
		GL.glVertexPointer(size, type, stride, buffer, Memory.getPosition(buffer));
	}

	public void glPolygonMode (int face, int mode) {
		GL.glPolygonMode(face, mode);
	}
}
