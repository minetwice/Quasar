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
		int maxAllowed = net.quasar.mobile.QuasarCapabilities.getMaxDrawBuffers();
		if (buffers.length > maxAllowed) {
			net.irisshaders.iris.Iris.logger.warn("Draw buffers requested (" + buffers.length + ") exceeds max supported (" + maxAllowed + "), clamping extra buffers.");
		}

		int count = Math.min(buffers.length, maxAllowed);
		int[] glBuffers = new int[count];
		int index = 0;

		for (int i = 0; i < count; i++) {
			int buffer = buffers[i];
			if (buffer >= maxColorAttachments) {
				glBuffers[index++] = GL30C.GL_NONE;
			} else {
				glBuffers[index++] = GL30C.GL_COLOR_ATTACHMENT0 + buffer;
			}
		}

		IrisRenderSystem.drawBuffers(getGlId(), glBuffers);
	}

	public void clearAttachments() {
		bind();
		for (int attachmentIndex : attachments.keySet()) {
			IrisRenderSystem.clearBufferfv(getGlId(), GL30C.GL_COLOR, attachmentIndex, new float[]{0.0f, 0.0f, 0.0f, 0.0f});
		}
		if (hasDepthAttachment) {
			IrisRenderSystem.clearBufferfv(getGlId(), GL30C.GL_DEPTH, 0, new float[]{1.0f});
		}
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

		int status = IrisRenderSystem.checkFramebufferStatus(GL30C.GL_FRAMEBUFFER);
		if (status != GL30C.GL_FRAMEBUFFER_COMPLETE && net.quasar.mobile.QuasarContext.isGLES()) {
			for (int attempt = 1; attempt <= 3; attempt++) {
				net.irisshaders.iris.Iris.logger.warn("[Quasar] Framebuffer incomplete (" + status + "), attempting repair attempt " + attempt + "...");
				status = IrisRenderSystem.checkFramebufferStatus(GL30C.GL_FRAMEBUFFER);
				if (status == GL30C.GL_FRAMEBUFFER_COMPLETE) {
					net.irisshaders.iris.Iris.logger.info("[Quasar] FBO repaired successfully.");
					break;
				}
			}
		}

		return status;
	}

	public int getId() {
		return getGlId();
	}
}
