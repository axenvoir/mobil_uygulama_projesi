import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as Updates from 'expo-updates';

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    if (__DEV__) console.error("Uygulama Hatası:", error, errorInfo);
  }

  handleRestart = async () => {
    try {
      await Updates.reloadAsync();
    } catch (error) {
      if (__DEV__) console.error('Uygulama yeniden başlatılamadı:', error);
      this.setState({ hasError: false }); 
    }
  };

  render() {
    if (this.state.hasError) {
      return (
        <View style={styles.container}>
          <View style={styles.iconContainer}>
            <Ionicons name="warning" size={60} color="#EF4444" />
          </View>
          <Text style={styles.title}>Bir Sorun Oluştu</Text>
          <Text style={styles.subtitle}>
            Uygulama beklenmedik bir hatayla karşılaştı. Endişelenme, verilerin güvende!
          </Text>

          <TouchableOpacity style={styles.button} onPress={this.handleRestart}>
            <Ionicons name="refresh" size={20} color="#FFFFFF" />
            <Text style={styles.buttonText}>Uygulamayı Yeniden Başlat</Text>
          </TouchableOpacity>
        </View>
      );
    }

    return this.props.children;
  }
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A', justifyContent: 'center', alignItems: 'center', padding: 30 },
  iconContainer: { width: 100, height: 100, borderRadius: 50, backgroundColor: '#EF444420', justifyContent: 'center', alignItems: 'center', marginBottom: 20 },
  title: { fontSize: 24, fontWeight: 'bold', color: '#F8FAFC', marginBottom: 12 },
  subtitle: { fontSize: 14, color: '#94A3B8', textAlign: 'center', lineHeight: 22, marginBottom: 30 },
  button: { flexDirection: 'row', backgroundColor: '#14B8A6', paddingHorizontal: 24, paddingVertical: 14, borderRadius: 25, alignItems: 'center' },
  buttonText: { color: '#FFFFFF', fontSize: 16, fontWeight: 'bold', marginLeft: 8 }
});