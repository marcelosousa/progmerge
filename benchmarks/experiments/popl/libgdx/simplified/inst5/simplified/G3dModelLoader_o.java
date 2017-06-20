package com.badlogic.gdx.graphics.g3d.loader;

public class G3dModelLoader extends ModelLoader<AssetLoaderParameters<Model>> {
  public static  short VERSION_HI = 0;
  public static  short VERSION_LO = 1;
  protected  BaseJsonReader reader;
  
  private int parseMaterials (ModelData model, JsonValue json, String materialDir) {
    JsonValue materials = json.get("materials");
    if(materials == null) {
      // we should probably create some default material in this case
    }
    else {
      model.materials.ensureCapacity(materials.size());
      for (JsonValue material = materials.child(); material != null; material = material.next()) {
        ModelMaterial jsonMaterial = new ModelMaterial();
        
        String id = material.getString("id", null);
        if(id == null)
          throw new GdxRuntimeException("Material needs an id.");

        jsonMaterial.id(id);
              
        // Read material colors
         JsonValue diffuse = material.get("diffuse");
        if (diffuse != null)
          jsonMaterial.diffuse(parseColor(diffuse));
         JsonValue ambient = material.get("ambient");
        if (ambient != null)
          jsonMaterial.ambient(parseColor(ambient));
         JsonValue emissive= material.get("emissive");
        if (emissive!= null)
          jsonMaterial.emissive(parseColor(emissive));
         JsonValue specular = material.get("specular");
        if (specular!= null)
          jsonMaterial.specular(parseColor(specular));
        // Read shininess
        jsonMaterial.shininess(material.getFloat("shininess", 0.0f));
        // Read opacity
        jsonMaterial.opacity(material.getFloat("opacity", 1.0f));
        
        // Read textures
        JsonValue textures = material.get("textures");
        if(textures != null){
          for (JsonValue texture = textures.child(); texture != null; texture = texture.next()) {
            ModelTexture jsonTexture = new ModelTexture();
            
            String textureId = texture.getString("id", null);
            if(textureId == null)
              throw new GdxRuntimeException("Texture has no id.");
            jsonTexture.id(textureId);
            
            String fileName = texture.getString("filename", null);
            if(fileName == null)
              throw new GdxRuntimeException("Texture needs filename.");
            jsonTexture.fileName(materialDir + (((materialDir.length() == 0) || (materialDir.endsWith("/") == 0)) ? "" : "/") + fileName);
            
            jsonTexture.uvTranslation(readVector2(texture.get("uvTranslation"), 0f, 0f));
            jsonTexture.uvScaling(readVector2(texture.get("uvScaling"), 1f, 1f));
            
            String textureType = texture.getString("type", null);
            if(textureType == null)
              throw new GdxRuntimeException("Texture needs type.");
            
            jsonTexture.usage(parseTextureUsage(textureType));
            
            if(jsonMaterial.textures == null)
              jsonMaterial.textures(new Array<ModelTexture>());
            jsonMaterial.textures.add(jsonTexture);
          }
        }

        model.materials.add(jsonMaterial);
      }
    }
    return 0;
  }
}
