import type {NavigatorScreenParams} from '@react-navigation/native';

export type MainTabsParamList = {
  Home: undefined;
  Products: undefined;
  Notes: undefined;
  Statistics: undefined;
  Profile: undefined;
};

export interface ProductIdScreenParams {
  id: string;
}

export type RootStackParamList = {
  MainTabs: NavigatorScreenParams<MainTabsParamList>;
  ProductDetail: ProductIdScreenParams;
  ProductForm: ProductIdScreenParams | undefined;
};

