import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import InstalledAppsScreen from '../screens/InstalledAppsScreen';

const Stack = createNativeStackNavigator();

const AppNavigator = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen 
          name="InstalledApps" 
          component={InstalledAppsScreen}
          options={{
            title: 'Aplicaciones Instaladas',
            headerStyle: {
              backgroundColor: '#007AFF',
            },
            headerTintColor: '#fff',
          }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default AppNavigator; 