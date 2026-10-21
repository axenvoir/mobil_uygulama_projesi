import 'react-native-gesture-handler';
import React, { useContext, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, StatusBar } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createStackNavigator } from '@react-navigation/stack';
import { AppProvider, AppContext } from './src/context/AppContext';
import { ThemeProvider } from './src/context/ThemeContext';
import CustomTabBar from './src/components/CustomTabBar';
import Toast from './src/components/Toast';
import LoadingScreen from './src/components/LoadingScreen';
import ErrorBoundary from './src/components/ErrorBoundary';
import BiometricLockScreen from './src/screens/BiometricLockScreen';
import { useTheme } from './src/hooks/useTheme';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';

import HomeScreen from './src/screens/HomeScreen';
import ProductsScreen from './src/screens/ProductsScreen';
import ProductDetailScreen from './src/screens/ProductDetailScreen';
import AddProductScreen from './src/screens/AddProductScreen';
import EditProductScreen from './src/screens/EditProductScreen';
import NotesScreen from './src/screens/NotesScreen';
import AddNoteScreen from './src/screens/AddNoteScreen';
import NotificationsScreen from './src/screens/NotificationsScreen';
import StatisticsScreen from './src/screens/StatisticsScreen';
import ProfileScreen from './src/screens/ProfileScreen';
import CategoriesScreen from './src/screens/CategoriesScreen';
import HelpScreen from './src/screens/HelpScreen';
import BackupScreen from './src/screens/BackupScreen';

const Tab = createBottomTabNavigator();
const Stack = createStackNavigator();

function MainTabs() {
  return (
    <Tab.Navigator tabBar={(props) => <CustomTabBar {...props} />} screenOptions={{ headerShown: false }}>
      <Tab.Screen name="Home" component={HomeScreen} />
      <Tab.Screen name="Products" component={ProductsScreen} />
      <Tab.Screen name="AddProduct" component={AddProductScreen} />
      <Tab.Screen name="Notes" component={NotesScreen} />
      <Tab.Screen name="Notifications" component={NotificationsScreen} />
    </Tab.Navigator>
  );
}

function AppContent() {
  const { isLoading, loadError, retryLoadData, toast, userProfile, isLocked, lockApp, hideToast } = useContext(AppContext);
  const { colors, isDark } = useTheme();

  useEffect(() => {
    if (!isLoading && userProfile.biometricsEnabled) {
      lockApp();
    }
  }, [isLoading, userProfile.biometricsEnabled]);

  if (isLoading) return <LoadingScreen />;
  if (loadError) {
    return (
      <SafeAreaView edges={['top']} style={[styles.loadErrorContainer, { backgroundColor: colors.background }]}>
        <View style={styles.loadErrorContent}>
          <Text style={[styles.loadErrorTitle, { color: colors.text }]}>Kayıtlı veriler açılamadı</Text>
          <Text style={[styles.loadErrorMessage, { color: colors.textLight }]}>
            Verilerin korunması için uygulama boş kayıtlarla açılmadı. İnternet gerekmez; tekrar deneyin. Sorun sürerse uygulamayı silmeyin.
          </Text>
          <TouchableOpacity
            accessibilityRole="button"
            style={[styles.retryButton, { backgroundColor: colors.primary }]}
            onPress={retryLoadData}
          >
            <Text style={[styles.retryButtonText, { color: colors.onPrimary }]}>Tekrar Dene</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }
  if (isLocked) return <BiometricLockScreen />;

  return (
    <View style={styles.root}>
      <StatusBar
        barStyle={isDark ? 'light-content' : 'dark-content'}
        backgroundColor={colors.background}
        translucent={false}
      />
      <SafeAreaView edges={['top']} style={[styles.navigatorArea, { backgroundColor: colors.background }]}>
        <NavigationContainer>
          <Stack.Navigator screenOptions={{ headerShown: false }}>
            <Stack.Screen name="MainTabs" component={MainTabs} />
            <Stack.Screen name="ProductDetail" component={ProductDetailScreen} />
            <Stack.Screen name="EditProduct" component={EditProductScreen} />
            <Stack.Screen name="AddNote" component={AddNoteScreen} />
            <Stack.Screen name="Statistics" component={StatisticsScreen} />
            <Stack.Screen name="Profile" component={ProfileScreen} />
            <Stack.Screen name="Categories" component={CategoriesScreen} />
            <Stack.Screen name="Help" component={HelpScreen} />
            <Stack.Screen name="Backup" component={BackupScreen} />
          </Stack.Navigator>
        </NavigationContainer>
      </SafeAreaView>
      <Toast toast={toast} onHide={hideToast} />
    </View>
  );
}

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <ErrorBoundary>
          <ThemeProvider>
            <AppProvider>
              <AppContent />
            </AppProvider>
          </ThemeProvider>
        </ErrorBoundary>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  navigatorArea: { flex: 1 },
  loadErrorContainer: { flex: 1, justifyContent: 'center', padding: 24 },
  loadErrorContent: { alignItems: 'center' },
  loadErrorTitle: { fontSize: 22, fontWeight: '800', textAlign: 'center', marginBottom: 12 },
  loadErrorMessage: { fontSize: 16, lineHeight: 24, textAlign: 'center', marginBottom: 24 },
  retryButton: { minHeight: 48, paddingHorizontal: 24, borderRadius: 24, justifyContent: 'center' },
  retryButtonText: { fontSize: 16, fontWeight: '700' },
});