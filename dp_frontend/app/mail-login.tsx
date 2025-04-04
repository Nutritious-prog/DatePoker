import React, { useState } from "react";
import { View, Text, TextInput, TouchableOpacity, Image, KeyboardAvoidingView, Platform } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { GestureHandlerRootView, ScrollView } from "react-native-gesture-handler";
import images from "@/constants/images";
import icons from "@/constants/icons";

const Login = () => {
    const router = useRouter();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleLogin = () => {
        console.log("Logging in with:", email, password);
    };

    return (
        <GestureHandlerRootView style={{ flex: 1 }}>
            <KeyboardAvoidingView
                behavior={Platform.OS === "ios" ? "padding" : "height"}
                className="flex-1"
                keyboardVerticalOffset={Platform.OS === "ios" ? 0 : -100} // reduce extra white space
            >
                <SafeAreaView className="bg-background flex-1">
                    <ScrollView
                        contentContainerStyle={{ flexGrow: 1, justifyContent: "center", paddingBottom: 20 }}
                        keyboardShouldPersistTaps="handled"
                    >
                        {/* Header illustration */}
                        <View className="items-center mt-5">
                            <Image
                                source={images.signInCouples}
                                className="w-full h-96"
                                resizeMode="contain"
                            />
                        </View>

                        {/* Login form */}
                        <View className="px-8 mt-4">
                            <Text className="text-center text-black-200 text-base font-rubik">
                                Sign Up to Date Poker with e-mail
                            </Text>

                            {/* Email input */}
                            <View className="bg-white rounded-full mt-6 px-5 py-4 border border-primary-300">
                                <Text className="text-primary-300 font-rubik-bold mb-1">Email</Text>
                                <TextInput
                                    placeholder="you@example.com"
                                    keyboardType="email-address"
                                    value={email}
                                    onChangeText={setEmail}
                                    className="text-black-300 font-rubik"
                                />
                            </View>

                            {/* Password input */}
                            <View className="bg-white rounded-full mt-4 px-5 py-4 border border-primary-300">
                                <Text className="text-primary-300 font-rubik-bold mb-1">Password</Text>
                                <TextInput
                                    placeholder="••••••••"
                                    secureTextEntry
                                    value={password}
                                    onChangeText={setPassword}
                                    className="text-black-300 font-rubik"
                                />
                            </View>

                            {/* Login button */}
                            <TouchableOpacity
                                onPress={handleLogin}
                                className="bg-primary-300 py-4 mt-6 rounded-full items-center shadow"
                            >
                                <Text className="text-white font-rubik-bold text-base">LOGIN</Text>
                            </TouchableOpacity>

                            {/* Forgot password */}
                            <TouchableOpacity className="mt-4">
                                <Text className="text-xs text-center text-black-100">
                                    Forgot your password? <Text className="underline">Reset it here</Text>
                                </Text>
                            </TouchableOpacity>

                            {/* Go back button */}
                            <TouchableOpacity
                                onPress={() => router.back()}
                                className="flex flex-row justify-center items-center mt-6"
                            >
                                <Text className="text-black-200 text-base">◀ GO BACK</Text>
                            </TouchableOpacity>
                        </View>
                    </ScrollView>
                </SafeAreaView>
            </KeyboardAvoidingView>
        </GestureHandlerRootView>
    );
};

export default Login;