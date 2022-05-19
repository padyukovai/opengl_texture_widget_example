package com.yourcompany.opengltexture;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.view.TextureRegistry;

public class OpenglTexturePlugin implements FlutterPlugin, MethodCallHandler {
    public TextureRegistry textures;
    private LongSparseArray<OpenGLRenderer> renders = new LongSparseArray<>();

    private MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        textures = binding.getTextureRegistry();

        String channelName = "opengl_texture";
        try {
            channel =
                    (MethodChannel)
                            new MethodChannel(binding.getBinaryMessenger(),
                                    channelName);
        } catch (Exception ex) {
            Log.e("TAG", "Received exception while setting up PathProviderPlugin", ex);
        }

        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        Map<String, Number> arguments = (Map<String, Number>) call.arguments;
        Log.d("OpenglTexturePlugin", call.method + " " + call.arguments.toString());
        if (call.method.equals("create")) {
            TextureRegistry.SurfaceTextureEntry entry = textures.createSurfaceTexture();
            SurfaceTexture surfaceTexture = entry.surfaceTexture();

            int width = arguments.get("width").intValue();
            int height = arguments.get("height").intValue();
            surfaceTexture.setDefaultBufferSize(width, height);

            SampleRenderWorker worker = new SampleRenderWorker();
            OpenGLRenderer render = new OpenGLRenderer(surfaceTexture, worker);

            renders.put(entry.id(), render);

            result.success(entry.id());
        } else if (call.method.equals("dispose")) {
            long textureId = arguments.get("textureId").longValue();
            OpenGLRenderer render = renders.get(textureId);
            render.onDispose();
            renders.delete(textureId);
        } else {
            result.notImplemented();
        }
    }
}

