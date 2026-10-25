import Biometrics from 'react-native-biometrics';

const biometrics = new Biometrics();

export type BiometricCapability = {
  available: boolean;
  biometryType: 'Fingerprint' | 'FaceID' | 'Iris' | 'None';
};

export const getBiometricCapability = async (): Promise<BiometricCapability> => {
  try {
    const {available, biometryType} = await biometrics.isSensorAvailable();
    return {
      available: Boolean(available),
      biometryType: (biometryType as BiometricCapability['biometryType']) ?? 'None',
    };
  } catch {
    return {available: false, biometryType: 'None'};
  }
};

export const authenticateBiometric = async (promptMessage: string): Promise<boolean> => {
  try {
    const {success} = await biometrics.simplePrompt({promptMessage});
    return Boolean(success);
  } catch {
    return false;
  }
};
