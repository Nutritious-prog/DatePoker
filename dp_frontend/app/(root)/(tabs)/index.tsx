import { Text, View } from "react-native";
import {Link} from "expo-router";

export default function Index() {
    const propertyId = "123"; // Example ID

    return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
        <Text className="font-bold text-3xl my-5 font-rubik"> Welcome to ReState</Text>

        <Link href="/sign-in">Sign In</Link>
        <Link href="/mail-login">Mail login</Link>
        <Link href="/mail-register">Mail register</Link>
        <Link href="/reset-password">Reset Password</Link>
        <Link href="/home">Home</Link>
        <Link href="/profile">Profile</Link>

    </View>
  );
}
