import { Directory, File, Paths } from 'expo-file-system';

const PRODUCT_IMAGES_DIR = 'product-images';

const ensureProductImagesDir = () => {
  const directory = new Directory(Paths.document, PRODUCT_IMAGES_DIR);
  if (!directory.exists) {
    directory.create({ intermediates: true, idempotent: true });
  }
  return directory;
};

const extensionFromUri = (uri) => {
  const match = String(uri).split('?')[0].match(/\.([a-zA-Z0-9]+)$/);
  if (!match) return '.jpg';
  const ext = match[1].toLowerCase();
  if (['jpg', 'jpeg', 'png', 'webp', 'heic', 'heif'].includes(ext)) {
    return `.${ext === 'jpeg' ? 'jpg' : ext}`;
  }
  return '.jpg';
};

/**
 * Copies a picker/camera cache URI into the app document directory so product
 * photos survive cache cleanup and app updates.
 */
export const persistProductImage = async (sourceUri) => {
  if (!sourceUri || typeof sourceUri !== 'string') return null;

  try {
    const documentRoot = Paths.document?.uri || '';
    if (documentRoot && sourceUri.startsWith(documentRoot) && sourceUri.includes(`/${PRODUCT_IMAGES_DIR}/`)) {
      return sourceUri;
    }

    const directory = ensureProductImagesDir();
    const filename = `product-${Date.now()}-${Math.random().toString(36).slice(2, 8)}${extensionFromUri(sourceUri)}`;
    const destination = new File(directory, filename);
    const source = new File(sourceUri);

    if (!source.exists) {
      if (__DEV__) if (__DEV__) console.warn('Ürün fotoğrafı kaynağı bulunamadı:', sourceUri);
      return null;
    }

    await source.copy(destination);
    return destination.uri;
  } catch (error) {
    if (__DEV__) if (__DEV__) console.error('Ürün fotoğrafı kalıcı depolamaya kopyalanamadı:', error);
    return null;
  }
};
