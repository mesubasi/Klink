import * as React from "react";
import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-xl text-xs font-semibold transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-950/20 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 cursor-pointer active:scale-[0.98]",
  {
    variants: {
      variant: {
        default:
          "bg-zinc-900 text-white shadow-xs hover:bg-zinc-800 active:bg-zinc-950 border border-zinc-800",
        blue:
          "bg-blue-600 text-white shadow-xs hover:bg-blue-700 active:bg-blue-800",
        destructive:
          "bg-red-600 text-white shadow-xs hover:bg-red-700 active:bg-red-800",
        outline:
          "border border-zinc-200/90 bg-white text-zinc-800 shadow-xs hover:bg-zinc-50 hover:text-zinc-950 hover:border-zinc-300",
        secondary:
          "bg-zinc-100 text-zinc-800 hover:bg-zinc-200/80 hover:text-zinc-900 border border-transparent",
        ghost:
          "text-zinc-600 hover:bg-zinc-100 hover:text-zinc-900",
        link:
          "text-blue-600 underline-offset-4 hover:underline",
      },
      size: {
        default: "h-9 px-4 py-2",
        sm: "h-8 rounded-lg px-3 text-xs",
        lg: "h-11 rounded-xl px-5 text-sm",
        xl: "h-12 rounded-xl px-6 text-sm",
        icon: "h-8 w-8 rounded-lg p-0",
        iconSm: "h-7 w-7 rounded-md p-0",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
);

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button";
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    );
  }
);
Button.displayName = "Button";

export { Button, buttonVariants };
