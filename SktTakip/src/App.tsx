import React from 'react';
import {StatusBar} from 'react-native';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {SafeAreaProvider} from 'react-native-safe-area-context';
import {AppProvider, useApp} from './context/AppContext';
import {ErrorBoundary, LoadingScreen} from './components';
import {MainTabs} from './navigation/MainTabs';
import {ProductDetailScreen} from './screens/ProductDetailScreen';
import {ProductFormScreen} from './screens/ProductFormScreen';
import {COLORS} from './theme';
import type {RootStackParamList} from './navigation/types';

const Stack = createNativeStackNavigator<RootStackParamList>();

const AppNavigator: React.FC = () => {
  const {ready} = useApp();
  if (!ready) {
    return <LoadingScreen />;
  }
  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{headerShown: false}}>
        <Stack.Screen name="MainTabs" component={MainTabs} />
        <Stack.Screen name="ProductDetail" component={ProductDetailScreen} />
        <Stack.Screen name="ProductForm" component={ProductFormScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

const App: React.FC = () => (
  <ErrorBoundary>
    <SafeAreaProvider>
      <StatusBar barStyle="light-content" backgroundColor={COLORS.primaryDark} />
      <AppProvider>
        <AppNavigator />
      </AppProvider>
    </SafeAreaProvider>
  </ErrorBoundary>
);

export default App;
