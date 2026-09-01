import * as React from "react";
import { cn } from "@/lib/utils";

export interface InputProps
  extends React.InputHTMLAttributes<HTMLInputElement> {}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, ...props }, ref) => {
    return (
      <input
        type={type}
        className={cn(
          "flex h-9 w-full rounded-xl border border-zinc-200/90 bg-white px-3 py-1.5 text-xs text-zinc-900 shadow-2xs transition-all placeholder:text-zinc-400 focus-visible:outline-none focus-visible:border-zinc-900 focus-visible:ring-2 focus-visible:ring-zinc-900/10 disabled:cursor-not-allowed disabled:opacity-50",
          className
        )}
        ref={ref}
        {...props}
      />
    );
  }
);
Input.displayName = "Input";

export { Input };
