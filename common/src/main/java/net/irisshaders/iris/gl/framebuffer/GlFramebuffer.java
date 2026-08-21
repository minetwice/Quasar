package net.irisshaders.iris.gl.framebuffer;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.irisshaders.iris.gl.GlResource;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.irisshaders.iris.pbr.TextureInfoCache;
import org.lwjgl.opengl.GL30C;

import java.util.ArrayList;
import java.util.List;

public class GlFramebuffer extends GlResource {
	private final Int2IntMap attachments;
	private final int maxDrawBuffers;
	private final int maxColorAttachments;
	private boolean hasDepthAttachment;

	public GlFramebuffer() {
		super(IrisRenderSystem.createFramebuffer());

		this.attachments = new Int2IntArrayMap();
		this.maxDrawBuffers = GlStateManager._getInteger(GL30C.GL_MAX_DRAW_BUFFERS);
		this.maxColorAttachments = GlStateManager._getInteger(GL30C.GL_MAX_COLOR_ATTACHMENTS);
		this.hasDepthAttachment = false;
	}

	public void addDepthAttachment(GpuTexture texture) {
		int fb = getGlId();

		// TODO: NeoForge 1.21.5
		//if (texture.getFormat().hasStencilAspect()) {
		//	IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_STENCIL_ATTACHMENT, GL30C.GL_TEXTURE_2D, texture, 0);
		//} else {
			IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, ((GlTexture) texture).glId(), 0);
		//}

		this.hasDepthAttachment = true;
	}

	public void addDepthAttachmentBypass(int texture) {
		int fb = getGlId();

		IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, texture, 0);

		this.hasDepthAttachment = true;
	}

	public void addColorAttachment(int index, int texture) {
		int fb = getGlId();

		IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0 + index, GL30C.GL_TEXTURE_2D, texture, 0);
		attachments.put(index, texture);
	}

	public void noDrawBuffers() {
		IrisRenderSystem.drawBuffers(getGlId(), new int[]{GL30C.GL_NONE});
	}

	public void drawBuffers(int[] buffers) {
		int[] glBuffers = new int[buffers.length];
		int index = 0;

		if (buffers.length > maxDrawBuffers) {
			throw new IllegalArgumentException("Cannot write to more than " + maxDrawBuffers + " draw buffers on this GPU");
		}

		int allowedBuffers = net.quasar.mobile.QuasarCapabilities.getInstance().getMaxDrawBuffers();
		List<Integer> validList = new ArrayList<>();

		for (int buffer : buffers) {
			if (buffer < maxColorAttachments && validList.size() < allowedBuffers) {
				validList.add(buffer);
			}
		}

		if (validList.isEmpty()) {
			validList.add(0);
		}

		glBuffers = new int[validList.size()];
		for (int i = 0; i < validList.size(); i++) {
			glBuffers[i] = GL30C.GL_COLOR_ATTACHMENT0 + validList.get(i);
		}

		IrisRenderSystem.drawBuffers(getGlId(), glBuffers);
		verifyFboStatus();
	}

	public void readBuffer(int buffer) {
		IrisRenderSystem.readBuffer(getGlId(), GL30C.GL_COLOR_ATTACHMENT0 + buffer);
	}

	public int getColorAttachment(int index) {
		return attachments.get(index);
	}

	public boolean hasDepthAttachment() {
		return hasDepthAttachment;
	}

	public void bind() {
		GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, getGlId());
	}

	public void bindAsReadBuffer() {
		GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, getGlId());
	}

	public void bindAsDrawBuffer() {
		GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, getGlId());
	}

	protected void destroyInternal() {
		GlStateManager._glDeleteFramebuffers(getGlId());
	}

	public int getStatus() {
		bind();

		return IrisRenderSystem.checkFramebufferStatus(GL30C.GL_FRAMEBUFFER);
	}

	public void verifyFboStatus() {
		bind();
		int status = IrisRenderSystem.checkFramebufferStatus(GL30C.GL_FRAMEBUFFER);
		int attempts = 0;
		while (status != GL30C.GL_FRAMEBUFFER_COMPLETE && attempts < 4 && net.quasar.mobile.QuasarContext.getInstance().isGLES()) {
			attempts++;
			org.slf4j.LoggerFactory.getLogger("Quasar").warn("[Quasar] FBO downgrade attempt " + attempts + ": status=0x" + Integer.toHexString(status));
			for (Int2IntMap.Entry entry : attachments.int2IntEntrySet()) {
				int colorIdx = entry.getIntKey();
				int oldTex = entry.getIntValue();
				int newTex = com.mojang.blaze3d.opengl.GlStateManager._genTexture();
				GlStateManager._bindTexture(newTex);
				GlStateManager._texImage2D(GL30C.GL_TEXTURE_2D, 0, GL30C.GL_RGBA8, 512, 512, 0, GL30C.GL_RGBA, GL30C.GL_UNSIGNED_BYTE, null);
				IrisRenderSystem.framebufferTexture2D(getGlId(), GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0 + colorIdx, GL30C.GL_TEXTURE_2D, newTex, 0);
				attachments.put(colorIdx, newTex);
				GlStateManager._deleteTexture(oldTex);
			}
			status = IrisRenderSystem.checkFramebufferStatus(GL30C.GL_FRAMEBUFFER);
		}
		if (status == GL30C.GL_FRAMEBUFFER_COMPLETE && net.quasar.mobile.QuasarContext.getInstance().isGLES()) {
			org.slf4j.LoggerFactory.getLogger("Quasar").info("[Quasar] FBO reached COMPLETE status");
		} else if (status != GL30C.GL_FRAMEBUFFER_COMPLETE) {
			org.slf4j.LoggerFactory.getLogger("Quasar").error("[Quasar] Framebuffer incomplete after retries: status=0x" + Integer.toHexString(status));
		}
	}

	public int getId() {
		return getGlId();
	}
}
