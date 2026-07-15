import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "InRideMart",
  description: "Discover useful products during your journey."
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
