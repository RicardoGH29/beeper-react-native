import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import InstalledAppsScreen from '../screens/InstalledAppsScreen';
import HomeScreen from '../screens/HomeScreen.tsx';
import { SafeAreaView } from 'react-native-safe-area-context';

const Stack = createNativeStackNavigator();
const Tab = createMaterialTopTabNavigator();

// Componente de navegación con pestañas superiores
const TopTabNavigator = () => {
    return (
        <Tab.Navigator
            screenOptions={{
                tabBarActiveTintColor: '#FFFFFF',
                tabBarInactiveTintColor: '#E0E0E0',
                tabBarStyle: { backgroundColor: '#007AFF' },
                tabBarIndicatorStyle: { backgroundColor: '#FFFFFF' },
                tabBarLabelStyle: { fontWeight: 'bold' },
            }}
        >
            <Tab.Screen
                name="HomeTab"
                component={HomeScreen}
                options={{ tabBarLabel: 'Home' }}
            />
            <Tab.Screen
                name="AppsTab"
                component={InstalledAppsScreen}
                options={{ tabBarLabel: 'Aplicaciones' }}
            />
        </Tab.Navigator>
    );
};

const AppNavigator = () => {
    return (
        <NavigationContainer>
            <SafeAreaView style={{ flex: 1 }}>
                <Stack.Navigator screenOptions={{ headerShown: false }}>
                    <Stack.Screen name="MainTabs" component={TopTabNavigator} />
                </Stack.Navigator>
            </SafeAreaView>
        </NavigationContainer>
    );
};

export default AppNavigator;
