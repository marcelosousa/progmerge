{
  JsonValue materials = json.get("materials");
  if (materials == null)
  {
  }
  else
  {
    model.materials.ensureCapacity(materials.size());
    for (JsonValue material = materials.child() ; material != null ; material = material.next())
    {
      ModelMaterial jsonMaterial = new ModelMaterial();
      String id = material.getString("id", null);
      if (id == null)
        throw new GdxRuntimeException("Material needs an id.");
      jsonMaterial.id = id;
      final JsonValue diffuse = material.get("diffuse");
      if (diffuse != null)
        jsonMaterial.diffuse = parseColor(diffuse);
      final JsonValue ambient = material.get("ambient");
      if (ambient != null)
        jsonMaterial.ambient = parseColor(ambient);
      final JsonValue emissive = material.get("emissive");
      if (emissive != null)
        jsonMaterial.emissive = parseColor(emissive);
      final JsonValue specular = material.get("specular");
      if (specular != null)
        jsonMaterial.specular = parseColor(specular);
      jsonMaterial.shininess = material.getFloat("shininess", 0.0F);
      jsonMaterial.opacity = material.getFloat("opacity", 1.0F);
      JsonValue textures = material.get("textures");
      if (textures != null)
      {
        for (JsonValue texture = textures.child() ; texture != null ; texture = texture.next())
        {
          ModelTexture jsonTexture = new ModelTexture();
          String textureId = texture.getString("id", null);
          if (textureId == null)
            throw new GdxRuntimeException("Texture has no id.");
          jsonTexture.id = textureId;
          String fileName = texture.getString("filename", null);
          if (fileName == null)
            throw new GdxRuntimeException("Texture needs filename.");
          jsonTexture.fileName = materialDir + (materialDir.length() == 0 || materialDir.endsWith("/") ? "" : "/") + fileName;
          jsonTexture.uvTranslation = readVector2(texture.get("uvTranslation"), 0.0F, 0.0F);
          jsonTexture.uvScaling = readVector2(texture.get("uvScaling"), 1.0F, 1.0F);
          String textureType = texture.getString("type", null);
          if (textureType == null)
            throw new GdxRuntimeException("Texture needs type.");
          jsonTexture.usage = parseTextureUsage(textureType);
          if (jsonMaterial.textures == null)
            jsonMaterial.textures = new Array<ModelTexture>();
          jsonMaterial.textures.add(jsonTexture);
        }
      }
      model.materials.add(jsonMaterial);
    }
  }
}