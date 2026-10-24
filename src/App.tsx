import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Header } from './components/Header';
import { CustomTabBar } from './components/CustomTabBar';
import { Toast } from './components/Toast';

import { HomeScreen } from './screens/HomeScreen';
import { ProductsScreen } from './screens/ProductsScreen';
import { AddProductModal } from './screens/AddProductModal';
import { NotesScreen } from './screens/NotesScreen';
import { NotificationsScreen } from './screens/NotificationsScreen';
import { StatisticsScreen } from './screens/StatisticsScreen';
import { CategoriesScreen } from './screens/CategoriesScreen';
import { ProfileScreen } from './screens/ProfileScreen';
import { ProductDetailModal } from './screens/ProductDetailModal';
import { EditProductModal } from './screens/EditProductModal';

const AppContent: React.FC = () => {
  const { activeTab } = useApp();

  const renderActiveScreen = () => {
    switch (activeTab) {
      case 'home':
        return <HomeScreen />;
      case 'products':
        return <ProductsScreen />;
      case 'add':
        return <AddProductModal />;
      case 'notes':
        return <NotesScreen />;
      case 'alerts':
        return <NotificationsScreen />;
      case 'stats':
        return <StatisticsScreen />;
      case 'categories':
        return <CategoriesScreen />;
      case 'profile':
        return <ProfileScreen />;
      default:
        return <HomeScreen />;
    }
  };

  return (
    <div className="min-h-screen bg-[#F5F7F4] dark:bg-[#101C19] text-[#19332D] dark:text-[#F0F4EC] transition-colors selection:bg-[#267968]/20 selection:text-[#267968]">
      {/* Top Header */}
      <Header />

      {/* Main Content Area */}
      <main className="max-w-2xl mx-auto px-4 pt-4 pb-20">
        {renderActiveScreen()}
      </main>

      {/* Floating Bottom Tab Bar */}
      <CustomTabBar />

      {/* Floating Toast Notification */}
      <Toast />

      {/* Overlays / Modals */}
      <ProductDetailModal />
      <EditProductModal />
    </div>
  );
};

export default function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}
